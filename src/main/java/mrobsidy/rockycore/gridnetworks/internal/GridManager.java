/**
 * 
 *  RockyCore
 *  Copyright (C) 2018-2019 MrObsidy
 *  
 *  
 *  This file is part of RockyCore.
 *
 *  RockyCore is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  RockyCore is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with RockyCore.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */


package mrobsidy.rockycore.gridnetworks.internal;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import mrobsidy.rockycore.gridnetworks.api.Grid;
import mrobsidy.rockycore.gridnetworks.api.IGridGenerator;
import mrobsidy.rockycore.gridnetworks.api.IGridNode;
import mrobsidy.rockycore.gridnetworks.api.IGridUser;
import mrobsidy.rockycore.init.RegistryRegistry;
import mrobsidy.rockycore.misc.BlockWrapper;
import mrobsidy.rockycore.misc.Debug;
import mrobsidy.rockycore.misc.MiscUtil;
import mrobsidy.rockycore.misc.backport.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

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
	
	public Class getManagerClass(){
		return this.gridType;
	}
	
	public ArrayList<Grid> getGrids(){
		return this.networks;
	}
	
	private void rebuild(ArrayList<Grid> networks){
		this.networks = networks;
	}
	
	public void register(Grid grid){
		networks.add(grid);
	}
	
	public void removeNodeFromNet(IGridNode node){
		Grid trg = getGridForPos(node.getPosition(), node.getDimension());
		trg.removeNode(node);
		if(trg.getNodes().size() == 0){
			networks.remove(trg);
		}
	}
	
	public void removeUserFromNet(IGridUser user){
		Grid trg = getGridForUser(user);
		trg.removeUser(user);;
	}
	
	public boolean addNodeToNet(IGridNode node){
		boolean ret = addNodeToNet(node, false);
		return ret;
	}
	
	public boolean addNodeToNet(IGridNode node, boolean noNewNet){
		BlockPos blockPos = node.getPosition();	
		boolean gridWasFound = false;
		
		int dim = node.getDimension();
		
		Debug.debug("Initiating node adding method");

		ArrayList<Grid> grids = new ArrayList<Grid>();
		ArrayList<Integer> iterates = new ArrayList<Integer>();
		
		BlockWrapper[] surBl = MiscUtil.getSurroundingBlocks(blockPos, dim);
		
		int iterate = 0;
		for(BlockWrapper block : surBl){
			if(block instanceof IGridNode){
				IGridNode parBlock = (IGridNode) block;
				grids.add(this.getGridForPos(block.getPos(), dim));
				gridWasFound = true;
				iterates.add(new Integer(iterate));
				
				int fliterate = iterate;
				
				EnumFacing direction = null;
				
				if (fliterate == 0){
					direction = EnumFacing.WEST;
				} else if (fliterate == 1){
					direction = EnumFacing.EAST;
				} else if (fliterate == 2){
					direction = EnumFacing.DOWN;
				} else if (fliterate == 3){
					direction = EnumFacing.UP;
				} else if (fliterate == 4){
					direction = EnumFacing.NORTH;
				} else if (fliterate == 5){
					direction = EnumFacing.SOUTH;
				}
				
				((IGridNode) block).setConnectingDirection(direction, true);
			}
			iterate++;
		}
		
		for(Integer fliterate : iterates){
			
			if (fliterate.intValue() == 0){
				node.setConnectingDirection(EnumFacing.EAST, true);
			} else if (fliterate.intValue() == 1){
				node.setConnectingDirection(EnumFacing.WEST, true);
			} else if (fliterate.intValue() == 2){
				node.setConnectingDirection(EnumFacing.SOUTH, true);
			} else if (fliterate.intValue() == 3){
				node.setConnectingDirection(EnumFacing.NORTH, true);
			} else if (fliterate.intValue() == 4){
				node.setConnectingDirection(EnumFacing.UP, true);
			} else if (fliterate.intValue() == 5){
				node.setConnectingDirection(EnumFacing.DOWN, true);
			} /* else {
				throw new IllegalStateException("GridManager " + this.toString() + " somehow received sixth direction!");
			} */
			else {
				Debug.debug("Somehow, GridManager " + this.toString() + " received a SEVENTH cardinal direction.");
			}
			
			if(iterate > 1) node.setConnectingNode(true);
			//node.setConnectingDirection();
		}
		
		Debug.debug("Grid was found: " + gridWasFound);
		
		if(gridWasFound && grids.size() == 1){
			Grid biggestGrid = null;
			int biggestGridSize = 0;
				
			for(Grid grid : grids){
				if(grid.getNodes().size() > biggestGridSize){
					biggestGrid = grid;
					biggestGridSize = grid.getNodes().size();
				}
			}
			biggestGrid.addNode(node);
			//Debug.debug("Added node"/* + node.getID() */ + " to Grid " + biggestGrid.ID);
		} else if (gridWasFound && grids.size() < 1){
			grids.get(0).addNode(node); //otherwise this node will be lost
			mergeGrids(grids);
		} else {
			if (!noNewNet){
				try {
					Constructor constr = this.gridType.getConstructor();
					try {
						Grid newGrid = (Grid) constr.newInstance();
						networks.add(newGrid);
						//newGrid.ID = networks.size();
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
		return gridWasFound;
	}
	
	public void addGridUserToNet(IGridUser user){
		user.setOrphan(false);
		
		BlockPos blockPos = user.getPos();	
		int dim = user.getDim();
		
		ArrayList<Grid> grids = new ArrayList<Grid>();
		
		boolean gridWasFound = false;
		
		BlockWrapper[] surBl = MiscUtil.getSurroundingBlocks(blockPos, dim);
		
		for(BlockWrapper block : surBl){
			if(block instanceof IGridNode){
				Grid grid = getGridForPos(block.getPos(), dim);
				gridWasFound = true;
				grids.add(grid);
			}
		}
		

//		
//		BlockPos[] surBp = new BlockPos[6]; 
//		
//		surBp[0] = new BlockPos(blockPos.getX() + 1, blockPos.getY(), blockPos.getZ());
//		surBp[1] = new BlockPos(blockPos.getX() - 1, blockPos.getY(), blockPos.getZ());
//		surBp[2] = new BlockPos(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ());
//		surBp[3] = new BlockPos(blockPos.getX(), blockPos.getY() - 1, blockPos.getZ());
//		surBp[4] = new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ() + 1);
//		surBp[5] = new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ() - 1);
//		
//		for(Grid network : networks){
//			for(IGridUser gridUser : network.getUsers()){
//				BlockPos pos = gridUser.getPos();
//				for (BlockPos surBpUnpacked : surBp){
//					if(pos.getX() == surBpUnpacked.getX() && pos.getY() == surBpUnpacked.getY() && pos.getZ() == surBpUnpacked.getZ()){
//						gridWasFound = true;
//						grids.add(network);
//					}
//				}
//			}
//		}
		
		
		if(gridWasFound){
			Grid biggestGrid = null;
			int biggestGridSize = 0;
				
			for(Grid grid : grids){
				if(grid.getNodes().size() > biggestGridSize){
					biggestGrid = grid;
					biggestGridSize = grid.getNodes().size();
				}
			}
			biggestGrid.addUser(user);
		} else {
			user.setOrphan(true);
		}
	}

	
	public void addGridGeneratorToNet(IGridGenerator generator){
		
		BlockPos blockPos = generator.getPosit();
		int dim = generator.getDimen();
		
		ArrayList<Grid> grids = new ArrayList<Grid>();
		
		boolean gridWasFound = false;
		
		BlockWrapper[] surBl = MiscUtil.getSurroundingBlocks(blockPos, dim);
		
		for(BlockWrapper block : surBl){
			if(block instanceof IGridNode){
				Grid grid = getGridForPos(block.getPos(), dim);
				gridWasFound = true;
				grids.add(grid);
			}
		}
		
		if(gridWasFound){
			Grid biggestGrid = null;
			int biggestGridSize = 0;
				
			for(Grid grid : grids){
				if(grid.getNodes().size() > biggestGridSize){
					biggestGrid = grid;
					biggestGridSize = grid.getNodes().size();
				}
			}
			biggestGrid.addGenerator(generator);
		} else {
			//Do nothing
		}
	}
	
	/**
	 * 
	 * Merges two or more Grids. [WIP!]
	 * 
	 * @param grids
	 */
	private void mergeGrids(ArrayList<Grid> grids){
		Grid largestGrid = null;
		int largestGridSize = 0;
		
		ArrayList<IGridNode> nodes = new ArrayList<IGridNode>();
		
		for(Grid grid : grids){
			if(grid.getNodes().size() < largestGridSize){
				largestGrid = grid;
			} else {
				grids.remove(grid);
			}
		}
		
		for (IGridNode node: nodes){
			boolean success = addNodeToNet(node);
			if(!success){
				nodes.add(node);
			}
		}
		
	}
	
	public Grid getGridForPos(BlockPos blockPos, int dim){
		Grid grid = null;
		
		if(blockPos == null){
			throw new IllegalStateException("position is null! Proceeding to crash!");
		}
		
		Debug.debug("blockPos " + blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ());
		
		for(Grid network : networks){
			for(IGridNode gridNode : network.getNodes()){
				BlockPos pos = gridNode.getPosition();
				
				if(pos == null){
					throw new IllegalStateException("position is of type null! Crashing!");
				}
				
				if(pos.getX() == blockPos.getX() && pos.getY() == blockPos.getY() && pos.getZ() == blockPos.getZ() && dim == gridNode.getDimension()){
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
	
	public IGridNode getNodeForPos(BlockPos pos, int dim){
		IGridNode returnNode = null;
		
		for(Grid network: networks){
			for(IGridNode node : network.getNodes()){
				if(node.getPosition().getX() == pos.getX() && node.getPosition().getY() == pos.getY() && node.getPosition().getZ() == pos.getZ() && node.getDimension() == dim){
					returnNode = node;
				}
			}
		}
		
		if(returnNode == null){
			//Make something happen I guess
		}
		
		return returnNode;
	}
	
	public IGridUser getUserForPos(BlockPos pos, int dim){
		IGridUser returnUser = null;
		
		for(Grid network: networks){
			for(IGridUser user : network.getUsers()){
				if(user.getPos().getX() == pos.getX() && user.getPos().getY() == pos.getY() && user.getPos().getZ() == pos.getZ() && user.getDim() == dim){
					returnUser = user;
				}
			}
		}
		
		if(returnUser == null){
			//Make something happen I guess
		}
		
		return returnUser;
	}
	
	/*public NBTTagCompound SaveToDisk(){
		
	} */
}