package com.jelly.MightyMiner.gui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.config.aotv.AOTVWaypoints;
import com.jelly.MightyMiner.config.aotv.Root;
import com.jelly.MightyMiner.utils.BlockUtils.BlockUtils;
import gg.essential.elementa.ElementaVersion;
import gg.essential.elementa.UIComponent;
import gg.essential.elementa.WindowScreen;
import gg.essential.elementa.components.ScrollComponent;
import gg.essential.elementa.components.UIContainer;
import gg.essential.elementa.components.UIText;
import gg.essential.elementa.components.Window;
import gg.essential.elementa.components.input.UITextInput;
import gg.essential.elementa.constraints.*;
import gg.essential.elementa.constraints.animation.Animations;
import gg.essential.elementa.effects.OutlineEffect;
import gg.essential.vigilance.gui.settings.ButtonComponent;
import kotlin.Unit;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ReportedException;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

public class AOTVWaypointsGUI extends WindowScreen {

    public static class Waypoint {
        @Expose
        public String name;
        @Expose
        public int x;
        @Expose
        public int y;
        @Expose
        public int z;
        public UIComponent waypointsComponent;

        public Waypoint(String name, int x, int y, int z) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Waypoint(String name, BlockPos pos) {
            this.name = name;
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
        }
    }

    private UIComponent scrollComponent;

    public static class WaypointList {
        @Expose
        public boolean enabled;
        @Expose
        public String name;
        @Expose
        public boolean showCoords;
        @Expose
        public ArrayList<Waypoint> waypoints;

        public UIComponent enabledComponent;

        public WaypointList(String name) {
            this.enabled = false;
            this.showCoords = false;
            this.waypoints = new ArrayList<>();
            this.name = name;
        }

        public WaypointList(String name, boolean enabled, boolean showCoords, ArrayList<Waypoint> waypoints) {
            this.enabled = enabled;
            this.showCoords = showCoords;
            this.waypoints = waypoints;
            this.name = name;
        }
    }

    public AOTVWaypointsGUI() {
        super(ElementaVersion.V2, false, true, true);
        getWindow().clearChildren();
        LoadWaypoints();
        ShowGUI();
    }

    public static void SaveWaypoints() {
        // Save waypoints to config file
        String json = MightyMiner.gson.toJson(MightyMiner.aotvWaypoints);
        try {
            Files.write(Paths.get("./config/aotv_coords_mm.json"), json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void LoadWaypoints() {
        // Load waypoints from config file
        try {
            String json = new String(Files.readAllBytes(Paths.get("./config/aotv_coords_mm.json")), StandardCharsets.UTF_8);
            MightyMiner.aotvWaypoints = MightyMiner.gson.fromJson(json, AOTVWaypoints.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onScreenClose() {
        System.out.println("Saving waypoints");
        super.onScreenClose();
        SaveWaypoints();
    }

    private void ShowGUI() {
        UIComponent topContainer = new UIContainer()
                .setWidth(new PixelConstraint(getWindow().getWidth()))
                .setHeight(new PixelConstraint(getWindow().getHeight() * 0.15f))
                .setX(new CenterConstraint())
                .setY(new PixelConstraint(0))
                .setChildOf(getWindow());

        new UIText("AOTV Waypoints")
                .setX(new CenterConstraint())
                .setY(new PixelConstraint(10))
                .setChildOf(topContainer);

        new ButtonComponent("Add New Waypoint List", this::AddNewWaypointListHandler)
                .setX(new CenterConstraint())
                .setY(new PixelConstraint(40))
                .setChildOf(topContainer);

        new ButtonComponent("Import routes from previous version (0.7 and older)", this::importFromPreviousVersion)
                .setX(new PixelConstraint(40))
                .setY(new AdditiveConstraint(new PixelConstraint(getWindow().getHeight()), new PixelConstraint(-30)))
                .setChildOf(getWindow());


        scrollComponent = new ScrollComponent("", 10f)
                .setX(new CenterConstraint())
                .setY(new PixelConstraint(getWindow().getHeight() * 0.15f))
                .setHeight(new AdditiveConstraint(new PixelConstraint(getWindow().getHeight() * 0.70f), new PixelConstraint(2)))
                .setWidth(new PixelConstraint(getWindow().getWidth() * 0.9f))
                .setChildOf(getWindow());


        for (WaypointList wl : MightyMiner.aotvWaypoints.getRoutes()) {
            UIComponent container = addNewWaypointList(wl);
            for (Waypoint w : wl.waypoints) {
                AddNewWaypoint(wl, w, container);
            }
        }
    }

    private Unit importFromPreviousVersion() {
        try {
            String data = (String) Toolkit.getDefaultToolkit()
                    .getSystemClipboard().getData(DataFlavor.stringFlavor);

            if (data == null || !data.startsWith("{") || !data.endsWith("}")) {
                return Unit.INSTANCE;
            }

            Root routes = MightyMiner.gson.fromJson(data, Root.class);

            if (routes.getRoutes() == null) {
                return Unit.INSTANCE;
            }

            System.out.println("Importing " + routes.getRoutes() + " routes");


            for (Map.Entry<String, JsonElement> route : routes.getRoutes().entrySet()) {
                String routeName = route.getKey();
                JsonObject waypoint = route.getValue().getAsJsonObject();

                if (waypoint == null) {
                    continue;
                }

                System.out.println("Importing route " + routeName);
                System.out.println("Waypoint: " + waypoint);

                ArrayList<Waypoint> waypoints = new ArrayList<>();

                for (Map.Entry<String, JsonElement> entry : waypoint.entrySet()) {
                    String waypointName = entry.getKey();
                    JsonObject waypointJson = entry.getValue().getAsJsonObject();
                    int x = waypointJson.get("x").getAsInt();
                    int y = waypointJson.get("y").getAsInt();
                    int z = waypointJson.get("z").getAsInt();
                    Waypoint w = new Waypoint(waypointName, x, y, z);
                    waypoints.add(w);
                }

                System.out.println("Importing " + waypoints.size() + " waypoints for route " + routeName);

                WaypointList waypointList = new WaypointList(routeName, false, false, waypoints);
                MightyMiner.aotvWaypoints.getRoutes().add(waypointList);
                UIComponent container = addNewWaypointList(waypointList);
                for (Waypoint w : waypointList.waypoints) {
                    AddNewWaypoint(waypointList, w, container);
                }
                SaveWaypoints();
            }

        } catch (UnsupportedFlavorException | IOException | IllegalArgumentException | ReportedException e) {
            System.out.println("Failed to import routes");
        }
        return Unit.INSTANCE;
    }

    private UIComponent addNewWaypointList(WaypointList wp) {
        UIComponent container = new UIContainer()
                .setChildOf(scrollComponent)
                .setX(new CenterConstraint())
                .setY(new SiblingConstraint(5f))
                .setWidth(new PixelConstraint(scrollComponent.getWidth() * 0.9f))
                .setHeight(new AdditiveConstraint(new ChildBasedRangeConstraint(), new PixelConstraint(15)))
                .enableEffect(new OutlineEffect(new Color(1f, 1f, 1f, 0.4f), 1f));

        wp.enabledComponent = new ButtonComponent(wp.enabled ? EnumChatFormatting.BOLD + "Selected" : "Select", () -> {
            if (!wp.enabled) {
                for (WaypointList wl1 : MightyMiner.aotvWaypoints.getRoutes()) {
                    if (wp.equals(wl1)) continue;
                    wl1.enabled = false;
                    if (wl1.enabledComponent != null) {
                        Window.Companion.enqueueRenderOperation(() -> {
                            wl1.enabledComponent.setColor(wl1.enabled ? Color.GREEN : Color.WHITE);
                            ((ButtonComponent) wl1.enabledComponent).setText(wl1.enabled ? EnumChatFormatting.BOLD + "Selected" : "Select");
                        });
                    }
                }
            }
            wp.enabled = !wp.enabled;
            MightyMiner.aotvWaypoints.enableRoute(wp);
            wp.enabledComponent.setColor(wp.enabled ? Color.GREEN : Color.WHITE);
            ((ButtonComponent) wp.enabledComponent).setText(wp.enabled ? (EnumChatFormatting.BOLD + "Selected") : "Select");
            return Unit.INSTANCE;
        })
                .setX(new PixelConstraint(7.5f))
                .setY(new PixelConstraint(7.5f))
                .setColor(wp.enabled ? Color.GREEN : Color.WHITE)
                .setChildOf(container);

        UIComponent name = new UITextInput(wp.name + " (" + wp.waypoints.size() + " waypoints)")
                .setTextScale(new PixelConstraint(1))
                .setX(new SiblingConstraint(10f))
                .setY(new PixelConstraint(12.5f))
                .setWidth(new PixelConstraint(scrollComponent.getWidth() * 0.6f))
                .setHeight(new PixelConstraint(24))
                .onMouseClick((component, clickType) -> {
                    if (clickType.getMouseButton() == 1) {
                        component.grabWindowFocus();
                    }
                    return Unit.INSTANCE;
                })
                .onKeyType((component, character, integer) -> {
                    wp.name = ((UITextInput) component).getText();
                    if (character == 13) {
                        component.releaseWindowFocus();
                    }
                    return Unit.INSTANCE;
                })
                .onFocusLost((component) -> {
                    setText(wp.name, true);
                    return Unit.INSTANCE;
                })
                .setChildOf(container);


        UIComponent expandButton = new ButtonComponent("Expand", () -> Unit.INSTANCE)
                .setX(new PixelConstraint(5f, true))
                .setY(new PixelConstraint(7.5f))
                .setChildOf(container);

        expandButton.onMouseClick((mouseButton, clickType) -> {
            if (clickType.getMouseButton() == 0) {
                Window.Companion.enqueueRenderOperation(() -> {
                    MightyMiner.aotvWaypoints.getRoutes().forEach(wl -> {
                        if (wp.equals(wl)) {
                            return;
                        }
                        if (wl.showCoords) {
                            wl.showCoords = false;
                            MightyMiner.aotvWaypoints.changeVisibility(wl, false);
                            for (Waypoint w : wl.waypoints) {
                                if (w.waypointsComponent != null) {
                                    w.waypointsComponent.hide(false);
                                }
                            }
                        }
                    });
                    wp.showCoords = !wp.showCoords;
                    MightyMiner.aotvWaypoints.changeVisibility(wp, wp.showCoords);
                    for (Waypoint w : wp.waypoints) {
                        if (w.waypointsComponent != null) {
                            if (wp.showCoords) {
                                w.waypointsComponent.unhide(false);
                            } else {
                                w.waypointsComponent.hide(false);
                            }
                        }
                    }
                });
            }
            return Unit.INSTANCE;
        });

        UIComponent deleteButton = new ButtonComponent("Delete", () -> Unit.INSTANCE)
                .setX(new SiblingConstraint(5f, true))
                .setY(new PixelConstraint(7.5f))
                .setChildOf(container);

        deleteButton.onMouseClick((mouseButton, clickType) -> {
            if (clickType.getMouseButton() == 0) {
                container.clearChildren();
                scrollComponent.removeChild(container);
                MightyMiner.aotvWaypoints.removeRoute(wp);
            }
            return Unit.INSTANCE;
        });

        UIComponent addWaypointButton = new ButtonComponent("Add Waypoint", () -> Unit.INSTANCE)
                .setX(new SiblingConstraint(5, true))
                .setY(new PixelConstraint(7.5f))
                .setChildOf(container);

        addWaypointButton.onMouseClick((mouseButton, clickType) -> {
            if (clickType.getMouseButton() == 0) {
                boolean done = MightyMiner.aotvWaypoints.addCoord(wp, new Waypoint(String.valueOf(wp.waypoints.size()), BlockUtils.getPlayerLoc().down()));
                if (!done) return Unit.INSTANCE;

                ((UITextInput) name).setText(wp.name + " (" + wp.waypoints.size() + " waypoints)");
                AddNewWaypoint(wp, wp.waypoints.get(wp.waypoints.size() - 1), container);
            }
            return Unit.INSTANCE;
        });

        container.getChildren().addObserver(((o, arg) -> {
            Window.Companion.enqueueRenderOperation(() -> {
                ((UITextInput) name).setText(wp.name + " (" + wp.waypoints.size() + " waypoints)");
            });
        }));

        return container;
    }

    private void AddNewWaypoint(WaypointList wl, Waypoint w, UIComponent mainContainer) {
        UIComponent container = new UIContainer()
                .setChildOf(mainContainer)
                .setX(new CenterConstraint())
                .setY(new SiblingConstraint(5f))
                .setWidth(new PixelConstraint(mainContainer.getWidth() * 0.95f))
                .setHeight(new AdditiveConstraint(new ChildBasedMaxSizeConstraint(), new PixelConstraint(10)))
                .enableEffect(new OutlineEffect(new Color(214f / 255f, 28f / 255f, 244f / 255f, 0.5f), 1f))
                .animateBeforeHide(animatingConstraints -> {
                    animatingConstraints.setHeightAnimation(Animations.OUT_SIN, 0.35f, new PixelConstraint(0));
                    return Unit.INSTANCE;
                })
                .animateAfterUnhide(animatingConstraints -> {
                    animatingConstraints.setHeightAnimation(Animations.IN_SIN, 0.35f, new AdditiveConstraint(new ChildBasedMaxSizeConstraint(), new PixelConstraint(10)));
                    return Unit.INSTANCE;
                });

        UIComponent containerName = new UIContainer()
                .setChildOf(container)
                .setX(new PixelConstraint(5f))
                .setY(new CenterConstraint())
                .setWidth(new PixelConstraint(container.getWidth() * 0.35f))
                .setHeight(new PixelConstraint(24));

        new UIText("Name:")
                .setTextScale(new PixelConstraint(1))
                .setX(new PixelConstraint(5f))
                .setY(new CenterConstraint())
                .setColor(Color.WHITE)
                .setChildOf(containerName);

        new UITextInput(w.name)
                .setChildOf(containerName)
                .setTextScale(new PixelConstraint(1.5f))
                .setX(new SiblingConstraint(7.5f))
                .setY(new CenterConstraint())
                .setHeight(new FillConstraint())
                .setWidth(new PixelConstraint(container.getWidth() * 0.3f))
                .onMouseClick((component, clickType) -> {
                    if (clickType.getMouseButton() == 1) {
                        component.grabWindowFocus();
                    }
                    return Unit.INSTANCE;
                })
                .onKeyType((component, character, integer) -> {
                    if (character == 13) {
                        component.releaseWindowFocus();
                    }
                    w.name = ((UITextInput) component).getText();
                    return Unit.INSTANCE;
                })
                .onFocusLost((component) -> {
                    setText(w.name, true);
                    return Unit.INSTANCE;
                });

        UIComponent containerX = new UIContainer()
                .setChildOf(container)
                .setX(new SiblingConstraint(5f))
                .setY(new CenterConstraint())
                .setWidth(new PixelConstraint(container.getWidth() * 0.1f))
                .setHeight(new PixelConstraint(24));

        new UIText("X:")
                .setTextScale(new PixelConstraint(1))
                .setX(new PixelConstraint(5f))
                .setY(new CenterConstraint())
                .setColor(Color.WHITE)
                .setChildOf(containerX);

        new UITextInput(String.valueOf(w.x))
                .setChildOf(containerX)
                .setTextScale(new PixelConstraint(1.5f))
                .setX(new SiblingConstraint(5f))
                .setY(new CenterConstraint())
                .setHeight(new FillConstraint())
                .setWidth(new PixelConstraint(container.getWidth() * 0.05f))
                .onMouseClick(((uiComponent, uiClickEvent) -> {
                    if (uiClickEvent.getMouseButton() == 1) {
                        uiComponent.grabWindowFocus();
                    }
                    return Unit.INSTANCE;
                }))
                .onKeyType((component, character, integer) -> {
                    if (character == 13) {
                        component.releaseWindowFocus();
                    }
                    try {
                        w.x = Integer.parseInt(((UITextInput) component).getText());
                    } catch (NumberFormatException e) {
                        w.x = 0;
                    }
                    return Unit.INSTANCE;
                })
                .onFocusLost((component) -> {
                    setText(String.valueOf(w.x), true);
                    return Unit.INSTANCE;
                });

        UIComponent containerY = new UIContainer()
                .setChildOf(container)
                .setX(new SiblingConstraint(5f))
                .setY(new CenterConstraint())
                .setWidth(new PixelConstraint(container.getWidth() * 0.1f))
                .setHeight(new PixelConstraint(24));

        new UIText("Y:")
                .setTextScale(new PixelConstraint(1))
                .setX(new PixelConstraint(5f))
                .setY(new CenterConstraint())
                .setColor(Color.WHITE)
                .setChildOf(containerY);

        new UITextInput(String.valueOf(w.y))
                .setChildOf(containerY)
                .setTextScale(new PixelConstraint(1.5f))
                .setX(new SiblingConstraint(5f))
                .setY(new CenterConstraint())
                .setHeight(new FillConstraint())
                .setWidth(new PixelConstraint(container.getWidth() * 0.05f))
                .onMouseClick(((uiComponent, uiClickEvent) -> {
                    if (uiClickEvent.getMouseButton() == 1) {
                        uiComponent.grabWindowFocus();
                    }
                    return Unit.INSTANCE;
                }))
                .onKeyType((component, character, integer) -> {
                    if (character == 13) {
                        component.releaseWindowFocus();
                    }
                    try {
                        w.y = Integer.parseInt(((UITextInput) component).getText());
                    } catch (NumberFormatException e) {
                        w.y = 0;
                    }
                    return Unit.INSTANCE;
                })
                .onFocusLost((component) -> {
                    setText(String.valueOf(w.y), true);
                    return Unit.INSTANCE;
                });

        UIComponent containerZ = new UIContainer()
                .setChildOf(container)
                .setX(new SiblingConstraint(5f))
                .setY(new CenterConstraint())
                .setWidth(new PixelConstraint(container.getWidth() * 0.1f))
                .setHeight(new PixelConstraint(24));

        new UIText("Z:")
                .setTextScale(new PixelConstraint(1))
                .setX(new PixelConstraint(5f))
                .setY(new CenterConstraint())
                .setColor(Color.WHITE)
                .setChildOf(containerZ);

        new UITextInput(String.valueOf(w.z))
                .setChildOf(containerZ)
                .setX(new SiblingConstraint(5f))
                .setTextScale(new PixelConstraint(1.5f))
                .setY(new CenterConstraint())
                .setHeight(new FillConstraint())
                .setWidth(new PixelConstraint(container.getWidth() * 0.05f))
                .onMouseClick(((uiComponent, uiClickEvent) -> {
                    if (uiClickEvent.getMouseButton() == 1) {
                        uiComponent.grabWindowFocus();
                    }
                    return Unit.INSTANCE;
                }))
                .onKeyType((component, character, integer) -> {
                    if (character == 13) {
                        component.releaseWindowFocus();
                    }
                    try {
                        w.z = Integer.parseInt(((UITextInput) component).getText());
                    } catch (NumberFormatException e) {
                        w.z = 0;
                    }
                    return Unit.INSTANCE;
                })
                .onFocusLost((component) -> {
                    setText(String.valueOf(w.z), true);
                    return Unit.INSTANCE;
                });

        UIComponent deleteButton = new ButtonComponent("Delete", () -> Unit.INSTANCE)
                .setX(new PixelConstraint(container.getWidth() * 0.85f))
                .setY(new CenterConstraint())
                .setChildOf(container);

        deleteButton.onMouseClick((mouseButton, clickType) -> {
            if (clickType.getMouseButton() == 0) {
                container.clearChildren();
                mainContainer.removeChild(container);
                MightyMiner.aotvWaypoints.removeCoord(wl, w);
            }
            return Unit.INSTANCE;
        });

        UIComponent verticalArrowsContainer = new UIContainer()
                .setChildOf(container)
                .setX(new PixelConstraint(container.getWidth() * 0.95f))
                .setY(new CenterConstraint())
                .setWidth(new PixelConstraint(20))
                .setHeight(new PixelConstraint(48));

        UIComponent upArrow = new ButtonComponent(" ▲ ", () -> Unit.INSTANCE)
                .setTextScale(new PixelConstraint(2.5f))
                .setX(new CenterConstraint())
                .setY(new PixelConstraint(0))
                .setChildOf(verticalArrowsContainer);

        upArrow.onMouseClick((mouseButton, clickType) -> {
            if (clickType.getMouseButton() == 0) {
                int index = wl.waypoints.indexOf(w);
                if (index > 0) {
                    wl.waypoints.remove(w);
                    wl.waypoints.add(index - 1, w);
                    mainContainer.removeChild(container);
                    mainContainer.insertChildAfter(container, mainContainer.getChildren().get(4 + index - 1));
                }
            }
            return Unit.INSTANCE;
        });

        UIComponent downArrow = new ButtonComponent(" ▼ ", () -> Unit.INSTANCE)
                .setTextScale(new PixelConstraint(2.5f))
                .setX(new CenterConstraint())
                .setY(new SiblingConstraint(5f))
                .setChildOf(verticalArrowsContainer);

        downArrow.onMouseClick((mouseButton, clickType) -> {
            if (clickType.getMouseButton() == 0) {
                int index = wl.waypoints.indexOf(w);
                if (index < wl.waypoints.size() - 1) {
                    wl.waypoints.remove(w);
                    wl.waypoints.add(index + 1, w);
                    mainContainer.removeChild(container);
                    mainContainer.insertChildAfter(container, mainContainer.getChildren().get(5 + index));
                }
            }
            return Unit.INSTANCE;
        });

        w.waypointsComponent = container;

        if (!wl.showCoords)
            Window.Companion.enqueueRenderOperation(() -> container.hide(true));
    }

    private Unit AddNewWaypointListHandler() {
        WaypointList route = MightyMiner.aotvWaypoints.addRoute("Waypoint List " + (MightyMiner.aotvWaypoints.getRoutes() != null ? MightyMiner.aotvWaypoints.getRoutes().size() : 1));
        addNewWaypointList(route);
        return Unit.INSTANCE;
    }
}


