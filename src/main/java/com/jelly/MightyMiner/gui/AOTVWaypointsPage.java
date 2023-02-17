package com.jelly.MightyMiner.gui;

import cc.polyfrost.oneconfig.gui.elements.BasicButton;
import cc.polyfrost.oneconfig.gui.elements.text.TextInputField;
import cc.polyfrost.oneconfig.gui.pages.Page;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.config.aotv.AOTVWaypointsStructs;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class AOTVWaypointsPage extends Page {

    private static final CopyOnWriteArrayList<TextInputField> textInputFields = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<TextInputField> nameInputFields = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<TextInputField> xInputFields = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<TextInputField> yInputFields = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<TextInputField> zInputFields = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<Route> routes = new CopyOnWriteArrayList<>();
    private final BasicButton addNewList = new BasicButton(120, BasicButton.SIZE_36, "Add New List", null, null, BasicButton.ALIGNMENT_CENTER, ColorPalette.PRIMARY);


    public AOTVWaypointsPage() {
        super("AOTV Waypoints");
        addNewList.setClickAction(() -> {
            if (MightyMiner.aotvWaypoints == null || MightyMiner.aotvWaypoints.getRoutes() == null) return;

            MightyMiner.aotvWaypoints.getRoutes().add(new AOTVWaypointsStructs.WaypointList("New Route", false, false, new ArrayList<>()));
            redrawRoutes();
        });
        redrawRoutes();
    }

    public static void redrawRoutes() {
        routes.clear();
        System.out.println("Redraw");
        if (MightyMiner.aotvWaypoints == null || MightyMiner.aotvWaypoints.getRoutes().size() == 0) return;

        textInputFields.clear();
        nameInputFields.clear();
        xInputFields.clear();
        yInputFields.clear();
        zInputFields.clear();

        for (AOTVWaypointsStructs.WaypointList list : MightyMiner.aotvWaypoints.getRoutes()) {
            Route route = new Route(list.name);
            route.setEnabled(list.enabled);
            route.setShowCoords(list.showCoords);

            if (list.showCoords && list.waypoints != null) {
                for (AOTVWaypointsStructs.Waypoint waypoint : list.waypoints) {
                    RouteWaypoint routeWaypoint = new RouteWaypoint();

                    TextInputField nameField = new TextInputField(150, 40, waypoint.name, false, false);
                    nameInputFields.add(nameField);
                    routeWaypoint.nameField = nameField;

                    TextInputField xField = new TextInputField(60, 40, String.valueOf(waypoint.x), false, false);
                    xInputFields.add(xField);
                    routeWaypoint.xField = xField;

                    TextInputField yField = new TextInputField(60, 40, String.valueOf(waypoint.y), false, false);
                    yInputFields.add(yField);
                    routeWaypoint.yField = yField;

                    TextInputField zField = new TextInputField(60, 40, String.valueOf(waypoint.z), false, false);
                    zInputFields.add(zField);
                    routeWaypoint.zField = zField;

                    BasicButton deleteButton = new BasicButton(80, BasicButton.SIZE_36, "Delete", null, null, BasicButton.ALIGNMENT_CENTER, ColorPalette.PRIMARY_DESTRUCTIVE);
                    deleteButton.setClickAction(() -> {
                        list.waypoints.remove(waypoint);
                        redrawRoutes();
                    });
                    routeWaypoint.deleteButton = deleteButton;

                    BasicButton moveUpButton = new BasicButton(30, BasicButton.SIZE_36, " ▲ ", null, null, BasicButton.ALIGNMENT_CENTER, ColorPalette.PRIMARY);
                    moveUpButton.setClickAction(() -> {
                        int index = list.waypoints.indexOf(waypoint);
                        if (index == 0) return;
                        list.waypoints.remove(waypoint);
                        list.waypoints.add(index - 1, waypoint);
                        redrawRoutes();
                    });
                    routeWaypoint.moveUpButton = moveUpButton;

                    BasicButton moveDownButton = new BasicButton(30, BasicButton.SIZE_36, " ▼ ", null, null, BasicButton.ALIGNMENT_CENTER, ColorPalette.PRIMARY);
                    moveDownButton.setClickAction(() -> {
                        int index = list.waypoints.indexOf(waypoint);
                        if (index == list.waypoints.size() - 1) return;
                        list.waypoints.remove(waypoint);
                        list.waypoints.add(index + 1, waypoint);
                        redrawRoutes();
                    });
                    routeWaypoint.moveDownButton = moveDownButton;

                    route.addWaypoint(routeWaypoint);
                }
            } else {
                route.waypoints = null;
            }

            BasicButton selected = new BasicButton(80, BasicButton.SIZE_36, list.enabled ? "Selected" : "Select", null, null, BasicButton.ALIGNMENT_CENTER, list.enabled ? ColorPalette.PRIMARY : ColorPalette.SECONDARY);
            if (list.enabled) {
                selected.setToggleable(false);
                selected.setToggled(true);
                selected.disable(true);
            } else {
                selected.setToggleable(true);
                selected.disable(false);
            }
            selected.setClickAction(() -> {
                MightyMiner.aotvWaypoints.getRoutes().forEach(waypointList -> waypointList.enabled = false);
                MightyMiner.aotvWaypoints.getRoutes().get(MightyMiner.aotvWaypoints.getRoutes().indexOf(list)).enabled = true;
                redrawRoutes();
            });
            route.selected = selected;

            route.nameField = new TextInputField(300, 40, list.name, false, false);
            textInputFields.add(route.nameField);

            BasicButton expandButton = new BasicButton(80, BasicButton.SIZE_36, list.showCoords ? "Hide" : "Expand", null, null, BasicButton.ALIGNMENT_CENTER, list.showCoords ? ColorPalette.PRIMARY : ColorPalette.SECONDARY);
            expandButton.setToggleable(true);
            expandButton.setClickAction(() -> {
                if (Objects.equals(expandButton.getText(), "Expand")) {
                    MightyMiner.aotvWaypoints.getRoutes().forEach(waypointList -> waypointList.showCoords = false);
                    MightyMiner.aotvWaypoints.getRoutes().get(MightyMiner.aotvWaypoints.getRoutes().indexOf(list)).showCoords = true;
                } else if (Objects.equals(expandButton.getText(), "Hide")) {
                    MightyMiner.aotvWaypoints.getRoutes().get(MightyMiner.aotvWaypoints.getRoutes().indexOf(list)).showCoords = false;
                }
                redrawRoutes();
            });
            route.expandButton = expandButton;

            BasicButton deleteButton = new BasicButton(80, BasicButton.SIZE_36, "Delete", null, null, BasicButton.ALIGNMENT_CENTER, ColorPalette.PRIMARY_DESTRUCTIVE);
            deleteButton.setClickAction(() -> {
                MightyMiner.aotvWaypoints.getRoutes().remove(list);
                redrawRoutes();
            });
            route.deleteButton = deleteButton;

            routes.add(route);
        }
    }

    @Override
    public void draw(long vg, int x, int y, InputHandler inputHandler) {
        int iX = x + 16;
        int iY = y + 64;

        if (MightyMiner.aotvWaypoints == null || MightyMiner.aotvWaypoints.getRoutes().size() == 0 || routes.size() == 0) {
            float widthOfText = NanoVGHelper.INSTANCE.getTextWidth(vg, "No routes found!", 24f, Fonts.BOLD);
            NanoVGHelper.INSTANCE.drawText(vg, "No routes found!", 512 - (widthOfText/2), 300, Color.WHITE.getRGB(), 24f, Fonts.BOLD);
            return;
        }

        for (Route list : routes) {
            NanoVGHelper.INSTANCE.drawRoundedRect(vg, iX, iY, 1008, 50, Color.DARK_GRAY.getRGB(), 12);

            list.selected.draw(vg, iX + 16, iY + 6, inputHandler);

            list.nameField.draw(vg, iX + 110, iY + 6, inputHandler);

            list.expandButton.draw(vg, x + 1008 - 80, iY + 6, inputHandler);

            list.deleteButton.draw(vg, x + 1008 - 80 - 90, iY + 6, inputHandler);

            if (list.showCoords) {
                int i = 0;
                for (RouteWaypoint waypoint : list.waypoints) {
                    int indendX = iX + 32;
                    NanoVGHelper.INSTANCE.drawRoundedRect(vg, indendX, iY + 56 + (i * 56), 944, 50, new Color(50,50,50, 120).getRGB(), 8);

                    indendX += 16;

                    NanoVGHelper.INSTANCE.drawText(vg, "Name: ", indendX, iY + 56 + (i * 56) + 6 + 20, Color.WHITE.getRGB(), 18f, Fonts.BOLD);
                    indendX += NanoVGHelper.INSTANCE.getTextWidth(vg, "Name: ", 18f, Fonts.BOLD) + 10;
                    waypoint.nameField.draw(vg, indendX, iY + 56 + (i * 56) + 6, inputHandler);

                    indendX += 160;
                    NanoVGHelper.INSTANCE.drawText(vg, "X: ", indendX, iY + 56 + (i * 56) + 6 + 20, Color.WHITE.getRGB(), 18f, Fonts.BOLD);
                    indendX += NanoVGHelper.INSTANCE.getTextWidth(vg, "X: ", 18f, Fonts.BOLD) + 10;
                    waypoint.xField.draw(vg, indendX, iY + 56 + (i * 56) + 6, inputHandler);
                    indendX += 70;
                    NanoVGHelper.INSTANCE.drawText(vg, "Y: ", indendX, iY + 56 + (i * 56) + 6 + 20, Color.WHITE.getRGB(), 18f, Fonts.BOLD);
                    indendX += NanoVGHelper.INSTANCE.getTextWidth(vg, "Y: ", 18f, Fonts.BOLD) + 10;
                    waypoint.yField.draw(vg, indendX, iY + 56 + (i * 56) + 6, inputHandler);
                    indendX += 70;
                    NanoVGHelper.INSTANCE.drawText(vg, "Z: ", indendX, iY + 56 + (i * 56) + 6 + 20, Color.WHITE.getRGB(), 18f, Fonts.BOLD);
                    indendX += NanoVGHelper.INSTANCE.getTextWidth(vg, "Z: ", 18f, Fonts.BOLD) + 10;
                    waypoint.zField.draw(vg, indendX, iY + 56 + (i * 56) + 6, inputHandler);

                    waypoint.moveUpButton.draw(vg, x + 944, iY + 56 + (i * 56) + 6, inputHandler);

                    waypoint.moveDownButton.draw(vg, x + 944 - 30 - 10, iY + 56 + (i * 56) + 6, inputHandler);

                    waypoint.deleteButton.draw(vg, x + 944 - 80 - 30 - 40, iY + 56 + (i * 56) + 6, inputHandler);

                    i++;
                }
                iY += (list.waypoints.size() * 56);
            }

            iY += 64;
        }
    }

    @Override
    public int drawStatic(long vg, int x, int y, InputHandler inputHandler) {
        addNewList.draw(vg, x + 8, y + 8, inputHandler);
        return addNewList.getHeight() + 16;
    }

    @Override
    public int getMaxScrollHeight() {
        int scrollHeight = addNewList.getHeight() + 32;
        for (Route list : routes) {
            scrollHeight += 64;
            if (list.showCoords) {
                scrollHeight += (list.waypoints.size() * 56);
            }
        }
        return scrollHeight;
    }

    @Override
    public void keyTyped(char key, int keyCode) {
        super.keyTyped(key, keyCode);
        textInputFields.forEach(textInputField -> {
            textInputField.keyTyped(key, keyCode);
            if (!textInputField.isToggled() && textInputField.getInput().length() > 0) {
                MightyMiner.aotvWaypoints.getRoutes().get(textInputFields.indexOf(textInputField)).name = textInputField.getInput();
                redrawRoutes();
            }
        });
        xInputFields.forEach(textInputField -> {
            textInputField.keyTyped(key, keyCode);
            if (!textInputField.isToggled() && textInputField.getInput().length() > 0) {
                try {
                    MightyMiner.aotvWaypoints.getRoutes().get(xInputFields.indexOf(textInputField)).waypoints.get(xInputFields.indexOf(textInputField)).x = Integer.parseInt(textInputField.getInput());
                    redrawRoutes();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        yInputFields.forEach(textInputField -> {
            textInputField.keyTyped(key, keyCode);
            if (!textInputField.isToggled() && textInputField.getInput().length() > 0) {
                try {
                    MightyMiner.aotvWaypoints.getRoutes().get(yInputFields.indexOf(textInputField)).waypoints.get(yInputFields.indexOf(textInputField)).y = Integer.parseInt(textInputField.getInput());
                    redrawRoutes();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        zInputFields.forEach(textInputField -> {
            textInputField.keyTyped(key, keyCode);
            if (!textInputField.isToggled() && textInputField.getInput().length() > 0) {
                try {
                    MightyMiner.aotvWaypoints.getRoutes().get(zInputFields.indexOf(textInputField)).waypoints.get(zInputFields.indexOf(textInputField)).z = Integer.parseInt(textInputField.getInput());
                    redrawRoutes();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        nameInputFields.forEach(textInputField -> {
            textInputField.keyTyped(key, keyCode);
            if (!textInputField.isToggled() && textInputField.getInput().length() > 0) {
                MightyMiner.aotvWaypoints.getRoutes().get(nameInputFields.indexOf(textInputField)).waypoints.get(nameInputFields.indexOf(textInputField)).name = textInputField.getInput();
                redrawRoutes();
            }
        });
    }

    private static class RouteWaypoint {
        private TextInputField nameField;
        private AOTVWaypointsStructs.Waypoint waypoint;
        private TextInputField xField;
        private TextInputField yField;
        private TextInputField zField;
        private BasicButton deleteButton;
        private BasicButton moveUpButton;
        private BasicButton moveDownButton;
    }

    private static class Route {
        private String name;
        private ArrayList<RouteWaypoint> waypoints = new ArrayList<>();
        private boolean enabled = false;
        private boolean showCoords = false;

        public BasicButton selected;
        public TextInputField nameField;
        public BasicButton expandButton;
        public BasicButton deleteButton;

        public Route(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public ArrayList<RouteWaypoint> getWaypoints() {
            return waypoints;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void addWaypoint(RouteWaypoint waypoint) {
            this.waypoints.add(waypoint);
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isShowCoords() {
            return showCoords;
        }

        public void setShowCoords(boolean showCoords) {
            this.showCoords = showCoords;
        }
    }
}
