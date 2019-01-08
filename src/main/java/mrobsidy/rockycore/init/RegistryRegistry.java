package mrobsidy.rockycore.init;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import mrobsidy.rockycore.gridnetworks.GridRegistry;
import mrobsidy.rockycore.misc.MiscRegistry;
import mrobsidy.rockycore.util.client.ClientRegistry;
import mrobsidy.rockycore.util.server.ServerRegistry;

/**
 * 
 * Here, all registries are registered.. 
 * (Horrible description, but that's what it does.)
 * 
 * This class is intended to be used to have common references along all of my mods.
 * @author mrobsidy
 *
 */
public class RegistryRegistry {
	private static ServerRegistry serverRegistry;
	private static ClientRegistry clientRegistry;
	private static MiscRegistry miscRegistry;
	private static GridRegistry gridRegistry;
	
	private static boolean serverRegistrySetForCurrentSession; //Set if the Server Registry is set for the current session
	
	private static boolean initFirstTime; //Marker if this wasn't initialized yet
	//Please note: "true" means that we already initialized. I made a funny when doing this.
	
	public static void setServerRegistry(MinecraftServer server){
		serverRegistry = new ServerRegistry(server);
	}
	
	@SideOnly(Side.CLIENT)
	public static void setClientRegistry(Minecraft client){
		clientRegistry = new ClientRegistry(client);
	}
	
	public static void constructMiscRegistry(){
		miscRegistry = new MiscRegistry();
	}
	
	public static void constructGridRegistry(){
		gridRegistry = new GridRegistry();
	}
	
	public static GridRegistry getGridRegistry(){
		return gridRegistry;
	}
	
	public static MiscRegistry getMiscRegistry(){
		return miscRegistry;
	}
	
	public static ServerRegistry getServerRegistry(){
		return serverRegistry;
	}
	
	@SideOnly(Side.CLIENT)
	public static ClientRegistry getClientRegistry(){
		return clientRegistry;
	}
	
	public static void initAndReset(){
		if(!initFirstTime) initFirstTime = false; //Initialize
		serverRegistrySetForCurrentSession = false; //Reset this
		serverRegistry = null; //in any case, reset the server registry
		gridRegistry = null;
		if(!initFirstTime && FMLCommonHandler.instance().getSide() == Side.CLIENT) clientRegistry = null; //if we're initializing and we are on a client, initialize it
		if(!initFirstTime) initFirstTime = true; //if this is the fist time initializing, set a marker that the next call of this function isn't the first time this function is being called
	}
}
