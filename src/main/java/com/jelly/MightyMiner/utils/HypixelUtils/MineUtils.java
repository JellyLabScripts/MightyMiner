package com.jelly.MightyMiner.utils.HypixelUtils;

import com.jelly.MightyMiner.MightyMiner;
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

    public static ArrayList<ArrayList<IBlockState>> getGemListBasedOnPriority(int priority) {
        ArrayList<ArrayList<IBlockState>> filter = new ArrayList<>();
        ArrayList<IBlockState> glass = new ArrayList<>();
        glass.add(Blocks.stained_glass.getDefaultState().withProperty(BlockColored.COLOR, gemPriority[priority]));
        filter.add(glass);
        if (MightyMiner.config.aotvMineGemstonePanes) {
            ArrayList<IBlockState> pane = new ArrayList<>();
            pane.add(Blocks.stained_glass_pane.getDefaultState().withProperty(BlockColored.COLOR, gemPriority[priority]));
            filter.add(pane);
        }
        return filter;
    }


    public static ArrayList<ArrayList<IBlockState>> getMithrilColorBasedOnPriority(int priority) {
        switch (priority) {
            case 0:
                return new ArrayList<ArrayList<IBlockState>>() {{
                    add(new ArrayList<IBlockState>() {{
                        add(Blocks.stained_hardened_clay.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.CYAN));
                        add(Blocks.wool.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY));
                    }});
                }};
            case 1:
                return new ArrayList<ArrayList<IBlockState>>() {{
                    add(new ArrayList<IBlockState>() {{
                        add(Blocks.prismarine.getDefaultState().withProperty(BlockPrismarine.VARIANT, BlockPrismarine.EnumType.BRICKS));
                        add(Blocks.prismarine.getDefaultState().withProperty(BlockPrismarine.VARIANT, BlockPrismarine.EnumType.DARK));
                        add(Blocks.prismarine.getDefaultState().withProperty(BlockPrismarine.VARIANT, BlockPrismarine.EnumType.ROUGH));
                    }});
                }};
            case 2:
                return new ArrayList<ArrayList<IBlockState>>() {{
                    add(new ArrayList<IBlockState>() {{
                        add(Blocks.wool.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.LIGHT_BLUE));
                    }});
                }};
            case 3:
                return new ArrayList<ArrayList<IBlockState>>() {{
                    add(new ArrayList<IBlockState>() {{
                        add(Blocks.stone.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE_SMOOTH));
                    }});
                }};
            default:
                return null;
        }
    }

}
