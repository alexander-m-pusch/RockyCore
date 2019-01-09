package mrobsidy.rockycore.gridnetworks.internal;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import mrobsidy.rockycore.gridnetworks.api.Grid;
import mrobsidy.rockycore.gridnetworks.api.IGridNode;
import mrobsidy.rockycore.gridnetworks.api.IGridUser;
import mrobsidy.rockycore.init.RegistryRegistry;
import mrobsidy.rockycore.misc.Debug;
import mrobsidy.rockycore.misc.MiscUtil;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class GridManager{
	private ArrayList<Grid> networks = new ArrayList<Grid>();
	
	public Class gridType;
	
	public final int ID;
	
	public GridManager(Class<? extends Grid> gridType){
		this.gridType = gridType;
		this.ID = RegistryRegistry.getGridRegistry().registerGridManager(this);
	}
	
	GridManager(Class<? extends Grid> gridType, int ID){
		this.gridType = gridType;
		this.ID = ID;
	}
	
	
	/**
	 * 
	 * Keep in mind that it's your job to reassemble the data on your own.
	 * 
	 * @param networks
	 * @return a new GridManager with everything added into it.
	 */
	public static GridManager reconstruct(ArrayList<Grid> networks, ArrayList<IGridNode> nodes, ArrayList<IGridUser> users){
		GridManager thisGM = new GridManager(networks.get(0).getClass());
		thisGM.rebuild(networks);
		return thisGM;
	}
	
	public Class getManagerClass(){
		return this.gridType;
	}
	
	public ArrayList<Grid> getGrids(){
		return this.networks;
	}
	
	private void rebuild(ArrayList<Grid> networks){
		this.networks = networks;
	}
	
	public int register(Grid grid){
		networks.add(grid);
		return networks.size() - 1;
	}
	
	public void addNodeToNet(IGridNode node){
		BlockPos blockPos = node.getPosition();	
		boolean gridWasFound = false;
		
		int dim = node.getDimension();
		
		Debug.debug("Initiating node adding method");

		ArrayList<Grid> grids = new ArrayList<Grid>();
		
		Block[] surBl = MiscUtil.getSurroundingBlocks(blockPos, dim);
		
		for(Block block : surBl){
			if(block instanceof IGridNode){
				grids.add(this.getGridForNode((IGridNode) block));
				gridWasFound = true;
			}
		}
		
		
		Debug.debug("Grid was found: " + gridWasFound);
		
		if(gridWasFound && grids.size() == 1){
			Grid biggestGrid = null;
			int biggestGridSize = 0;
				
			for(Grid grid : grids){
				if(grid.getNodesSize() > biggestGridSize){
					biggestGrid = grid;
					biggestGridSize = grid.getNodesSize();
				}
			}
			biggestGrid.addNode(node);
			Debug.debug("Added node " + node.getID() + " to Grid " + biggestGrid.ID);
		} else if (gridWasFound && grids.size() < 1){
			//TODO make this function
		} else {
			try {
				Constructor constr = this.gridType.getConstructor();
				try {
					Grid newGrid = (Grid) constr.newInstance();
					networks.add(newGrid);
					newGrid.ID = networks.size();
					newGrid.addNode(node);
					Debug.debug("Created a new network " + networks.get(networks.size() - 1));
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
			//networks.add();
		}
	}
	
	public Grid getGridForNode(IGridNode node){
		Grid grid = null;
			
		BlockPos blockPos = node.getPosition();
		
		for(Grid network : networks){
			for(IGridNode gridNode : network.getNodes()){
				BlockPos pos = gridNode.getPosition();
				if(pos.getX() == blockPos.getX() && pos.getY() == blockPos.getY() && pos.getZ() == blockPos.getZ() && node.getDimension() == gridNode.getDimension()){
					return network;
				}
			}
		}
		
		
		return grid;
	}
	
	public Grid getGridForUser(IGridUser user){
		Grid grid = null;
		
		BlockPos blockPos = user.getPos();
		
		for(Grid network : networks){
			for(IGridUser gridUser: network.getUsers()){
				BlockPos pos = gridUser.getPos();
				if(pos.getX() == blockPos.getX() && pos.getY() == blockPos.getY() && pos.getZ() == blockPos.getZ() && user.getDim() == gridUser.getDim()){
					return network;
				}
			}
		}
		
		
		return grid;
	}
	
	public void addGridUserToNet(IGridUser user){
		user.setOrphan(false);
		
		BlockPos blockPos = user.getPos();	
		int dim = user.getDim();
		
		/* Block[] surBl = MiscUtil.getSurroundingBlocks(blockPos, dim);
		
		for(Block block : surBl){
			if(block instanceof IGridNode){
				grids.add(((IGridNode) block).getGrid());
				gridWasFound = true;
			}
		}
		
		*/
		
		ArrayList<Grid> grids = new ArrayList<Grid>();
		
		boolean gridWasFound = false;
		
		BlockPos[] surBp = new BlockPos[6]; 
		
		surBp[0] = new BlockPos(blockPos.getX() + 1, blockPos.getY(), blockPos.getZ());
		surBp[1] = new BlockPos(blockPos.getX() - 1, blockPos.getY(), blockPos.getZ());
		surBp[2] = new BlockPos(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ());
		surBp[3] = new BlockPos(blockPos.getX(), blockPos.getY() - 1, blockPos.getZ());
		surBp[4] = new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ() + 1);
		surBp[5] = new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ() - 1);
		
		for(Grid network : networks){
			for(IGridUser gridUser : network.getUsers()){
				BlockPos pos = gridUser.getPos();
				for (BlockPos surBpUnpacked : surBp){
					if(pos.getX() == surBpUnpacked.getX() && pos.getY() == surBpUnpacked.getY() && pos.getZ() == surBpUnpacked.getZ()){
						gridWasFound = true;
						grids.add(network);
					}
				}
			}
		}
		
		
		if(gridWasFound){
			Grid biggestGrid = null;
			int biggestGridSize = 0;
				
			for(Grid grid : grids){
				if(grid.getNodesSize() > biggestGridSize){
					biggestGrid = grid;
					biggestGridSize = grid.getNodesSize();
				}
			}
			biggestGrid.addUser(user);
		} else {
			user.setOrphan(true);
		}
	}
	
	/*public NBTTagCompound SaveToDisk(){
		
	} */
}