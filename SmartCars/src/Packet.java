import java.util.UUID;

public class Packet {

    private String id;
    private double transTime;
    private static final int size = 1;
    private String vehicleId;
    private long packetBirthTime;
    private int serviceId;

    // Nothing much is there now in this class.
    public float storageDemand;
    public float computationDemand;

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getId() {
        return id;
    }

    public double getTransTime() {
        return transTime;
    }

    public void setTransTime(double addTime) { this.transTime += addTime; };

    public long getpacketBirthTime() { return packetBirthTime ;}

    public static int getSize() {
        return size;
    }

    Packet() {
        this.id = UUID.randomUUID().toString();
        this.transTime = 0;
        packetBirthTime = System.currentTimeMillis();
    }
}