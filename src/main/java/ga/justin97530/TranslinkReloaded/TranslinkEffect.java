package ga.justin97530.TranslinkReloaded;

/**
 * Created by justin on 07/10/16.
 */

public class TranslinkEffect {
    double speedMultiplier;
    boolean eject, destroy, message;


    public TranslinkEffect(double speedMultiplier, boolean eject, boolean destroy, boolean message) {
        this.speedMultiplier = speedMultiplier;
        this.eject = eject;
        this.destroy = destroy;
        this.message = message;
    }

    public TranslinkEffect clone() {
        return new TranslinkEffect(speedMultiplier, eject, destroy, message);
    }

    public boolean isMessage() {
        return message;
    }

    public void setMessage(boolean message) {
        this.message = message;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public void setSpeedMultiplier(double speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    public boolean isEject() {
        return eject;
    }

    public void setEject(boolean eject) {
        this.eject = eject;
    }

    public boolean isDestroy() {
        return destroy;
    }

    public void setDestroy(boolean destroy) {
        this.destroy = destroy;
    }
}
