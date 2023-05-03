package com.jelly.MightyMiner.utils.HypixelUtils;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.gui.AOTVGemstoneFilter;
import com.jelly.MightyMiner.utils.BlockUtils.BlockData;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockPrismarine;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import org.fusesource.jansi.Ansi;

import java.util.ArrayList;

public class MineUtils {

    public static EnumDyeColor[] gemPriority = {null, EnumDyeColor.RED, EnumDyeColor.PURPLE, EnumDyeColor.LIME, EnumDyeColor.LIGHT_BLUE, EnumDyeColor.ORANGE, EnumDyeColor.YELLOW, EnumDyeColor.MAGENTA};


    public static ArrayList<ArrayList<BlockData<?>>> getGemListBasedOnPriority(int priority) {
        ArrayList<ArrayList<BlockData<?>>> filter = new ArrayList<>();
        ArrayList<BlockData<?>> glass = new ArrayList<BlockData<?>>() {{
            add(new BlockData<>(Blocks.stained_glass, gemPriority[priority]));
        }};
        filter.add(glass);
        if (MightyMiner.config.aotvMineGemstonePanes) {
            ArrayList<BlockData<?>> panes = new ArrayList<BlockData<?>>() {{
                add(new BlockData<>(Blocks.stained_glass_pane, gemPriority[priority]));
            }};
            filter.add(panes);
        }
        return filter;
    }

    public static ArrayList<ArrayList<BlockData<?>>> getGemListBasedOnPriority() {
        ArrayList<ArrayList<BlockData<?>>> filter = new ArrayList<>();
        if (AOTVGemstoneFilter.ruby) {
            if (MightyMiner.config.aotvMineGemstonePanes) {
                filter.add(new ArrayList<BlockData<?>>() {{
                    add(new BlockData<>(Blocks.stained_glass, EnumDyeColor.RED));
                    add(new BlockData<>(Blocks.stained_glass_pane, EnumDyeColor.RED));
                }});
            } else {
                filter.add(new ArrayList<BlockData<?>>() {{
                    add(new BlockData<>(Blocks.stained_glass, EnumDyeColor.RED));
                }});
            }
        }
        if (AOTVGemstoneFilter.amethyst) {
            if (MightyMiner.config.aotvMineGemstonePanes) {
                filter.add(new ArrayList<BlockData<?>>() {{
                    add(new BlockData<>(Blocks.stained_glass, EnumDyeColor.PURPLE));
                    add(new BlockData<>(Blocks.stained_glass_pane, EnumDyeColor.PURPLE));
                }});
            } else {
                filter.add(new ArrayList<BlockData<?>>() {{
                    add(new BlockData<>(Blocks.stained_glass, EnumDyeColor.PURPLE));
                }});
            }
        }
        if (AOTVGemstoneFilter.topaz) {
            if (MightyMiner.config.aotvMineGemstonePanes) {
                filter.add(new ArrayList<BlockData<?>>() {{
                    add(new BlockData<>(Blocks.stained_glass, EnumDyeColor.YELLOW));
                    add(new BlockData<>(Blocks.stained_glass_pane, EnumDyeColor.YELLOW));
                }});
            } else {
                filter.add(new ArrayList<BlockData<?>>() {{
                    add(new BlockData<>(Blocks.stained_glass, EnumDyeColor.YELLOW));
                }});
            }
        }
        if (AOTVGemstoneFilter.sapphire) {
            if (MightyMiner.config.aotvMineGemstonePanes) {
                filter.add(new ArrayList<BlockData<?>>() {{
                    add(new BlockData<>(Blocks.stained_glass, EnumDyeColor.LIGHT_BLUE));
                    add(new BlockData<>(Blocks.stained_glass_pane, EnumDyeColor.LIGHT_BLUE));
                }});
            } else {
                filter.add(new ArrayList<BlockData<?>>() {{
                    add(new BlockData<>(Blocks.stained_glass, EnumDyeColor.LIGHT_BLUE));
                }});
            }
        }
        if (AOTVGemstoneFilter.amber) {
            if (MightyMiner.config.aotvMineGemstonePanes) {
                filter.add(new ArrayList<BlockData<?>>() {{
                    add(new BlockData<>(Blocks.stained_glass, EnumDyeColor.ORANGE));
                    add(new BlockData<>(Blocks.stained_glass_pane, EnumDyeColor.ORANGE));
                }});
            } else {
                filter.add(new ArrayList<BlockData<?>>() {{
                    add(new BlockData<>(Blocks.stained_glass, EnumDyeColor.ORANGE));
                }});
            }
        }
        if (AOTVGemstoneFilter.jade) {
            if (MightyMiner.config.aotvMineGemstonePanes) {
                filter.add(new ArrayList<BlockData<?>>() {{
                    add(new BlockData<>(Blocks.stained_glass, EnumDyeColor.LIME));
                    add(new BlockData<>(Blocks.stained_glass_pane, EnumDyeColor.LIME));
                }});
            } else {
                filter.add(new ArrayList<BlockData<?>>() {{
                    add(new BlockData<>(Blocks.stained_glass, EnumDyeColor.LIME));
                }});
            }
        }
        if (AOTVGemstoneFilter.jasper) {
            if (MightyMiner.config.aotvMineGemstonePanes) {
                filter.add(new ArrayList<BlockData<?>>() {{
                    add(new BlockData<>(Blocks.stained_glass, EnumDyeColor.MAGENTA));
                    add(new BlockData<>(Blocks.stained_glass_pane, EnumDyeColor.MAGENTA));
                }});
            } else {
                filter.add(new ArrayList<BlockData<?>>() {{
                    add(new BlockData<>(Blocks.stained_glass, EnumDyeColor.MAGENTA));
                }});
            }
        }
        return filter;
    }

    public static ArrayList<BlockData<?>> getMithrilColorBasedOnPriority(int priority) {
        switch (priority) {
            case 0:
                return new ArrayList<BlockData<?>>() {{
                    add(new BlockData<>(Blocks.stained_hardened_clay, null));
                    add(new BlockData<>(Blocks.wool, EnumDyeColor.GRAY));
                }};
            case 1:
                return new ArrayList<BlockData<?>>() {{
                    add(new BlockData<>(Blocks.prismarine, null));
                }};
            case 2:
                return new ArrayList<BlockData<?>>() {{
                    add(new BlockData<>(Blocks.wool, EnumDyeColor.LIGHT_BLUE));
                }};
            case 3:
                return new ArrayList<BlockData<?>>() {{
                    add(new BlockData<>(Blocks.stone.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE_SMOOTH).getBlock(), BlockStone.EnumType.DIORITE_SMOOTH));
                }};
            default:
                return null;
        }
    }

}
