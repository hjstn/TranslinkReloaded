package ga.justin97530.TranslinkReloaded;

import com.google.common.collect.Lists;
import org.apache.commons.lang.ObjectUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by justin on 07/10/16.
 */
public class TranslinkReloaded extends JavaPlugin implements Listener {
    private List<Material> rails = Arrays.asList(Material.RAILS, Material.POWERED_RAIL, Material.DETECTOR_RAIL, Material.ACTIVATOR_RAIL);
    private List<Material> signs = Arrays.asList(Material.SIGN, Material.SIGN_POST, Material.WALL_SIGN);
    private List<BlockFace> signFace = Arrays.asList(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.DOWN);

    private HashMap<Integer, Location> pastLocation = new HashMap<Integer, Location>();
    private HashMap<String, TranslinkEffect> translinkEffects = new HashMap<String, TranslinkEffect>();

    private String signFrom(Block block) {
        String message = null;

        for(BlockFace blockFace : signFace) {
            Block relativeBlock = block.getRelative(blockFace);
            if(signs.contains(relativeBlock.getType())) {
                message = String.join("", ((Sign) relativeBlock.getState()).getLines());
                break;
            }
        }

        return message == null ? null : ChatColor.translateAlternateColorCodes('&', message);
    }

    private void applyEffects(Block block, Vehicle vehicle, String effectName) {
        TranslinkEffect effect = translinkEffects.get(effectName);

        vehicle.setVelocity(vehicle.getVelocity().multiply(new Vector(effect.getSpeedMultiplier(), 1, effect.getSpeedMultiplier())));
        if(effect.isEject()) vehicle.eject();
        if(effect.isDestroy()) vehicle.remove();
        if(effect.isMessage() && !vehicle.isEmpty() && vehicle.getPassenger().getType() == EntityType.PLAYER) {
            String message = signFrom(block);
            if(message != null) vehicle.getPassenger().sendMessage(message);
        }
    }

    private Location locationRoundedOff(Location location) {
        location.setX(Math.floor(location.getX()));
        location.setY(Math.floor(location.getY()));
        location.setZ(Math.floor(location.getZ()));
        return location;
    }

    private boolean isBlock(Block block, List<Material> mat) {
        return mat.contains(block.getType());
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        Bukkit.getPluginManager().registerEvents(this, this);

        translinkEffects.put("default", new TranslinkEffect(1, false, false, false));

        ConfigurationSection effects = getConfig().getConfigurationSection("effects");
        for(String effectName : effects.getKeys(false)) {
            ConfigurationSection effect = effects.getConfigurationSection(effectName);
            String inherit = effects.getString("inherit");

            TranslinkEffect effectObject = (TranslinkEffect) translinkEffects.get(inherit != null ? inherit : "default").clone();

            effectObject.setSpeedMultiplier(effect.getDouble("speedMultiplier", effectObject.getSpeedMultiplier()));
            effectObject.setEject(effect.getBoolean("eject", effectObject.isEject()));
            effectObject.setDestroy(effect.getBoolean("destroy", effectObject.isDestroy()));
            effectObject.setMessage(effect.getBoolean("message", effectObject.message));

            translinkEffects.put(effectName, effectObject);
        }
    }

    @Override
    public void onDisable() {

    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onVehicleMove(VehicleMoveEvent e) {
        Vehicle vehicle = e.getVehicle();
        int vehicleID = vehicle.getEntityId();
        Location roundTo = locationRoundedOff(e.getTo());
        Block currentBlock = e.getFrom().getBlock();

        if(vehicle instanceof Minecart && isBlock(currentBlock, rails) && (!pastLocation.containsKey(vehicleID) || roundTo.distance(pastLocation.get(vehicleID)) >= 1)) {
            pastLocation.put(vehicle.getEntityId(), roundTo);
            Block baseBlock = currentBlock.getRelative(BlockFace.DOWN);
            String blockFormat = baseBlock.getType().name() + "-" + baseBlock.getData();
            ConfigurationSection modifierBlocks = getConfig().getConfigurationSection("modifierBlocks");

            String appliedEffect = "default";

            for(String modifierBlock : modifierBlocks.getKeys(false)) {
                if(blockFormat.equals(modifierBlock)) {
                    ConfigurationSection thisModifier = modifierBlocks.getConfigurationSection(modifierBlock);
                    if(baseBlock.isBlockIndirectlyPowered() && vehicle.isEmpty()) appliedEffect = thisModifier.getString("poweredEmptyEffect");
                    else if(baseBlock.isBlockIndirectlyPowered()) appliedEffect = thisModifier.getString("poweredEffect");
                    else if(vehicle.isEmpty()) appliedEffect = thisModifier.getString("emptyEffect");
                    else appliedEffect = thisModifier.getString("effect");
                }
            }

            applyEffects(baseBlock, vehicle, appliedEffect);

        }
    }

    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent e) {
        pastLocation.remove(e.getVehicle().getEntityId());
    }
}
