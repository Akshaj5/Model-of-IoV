import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

class Services {
    private String serviceId;
    private int computationDemand;
    private double transTimeServices;
    private long serviceBirthTime;
    private int storageDemand;
    private List<Packet> servicePackets = new ArrayList<>();

    int packetSize;

    int getComputationDemand(){
        return computationDemand;
    }
    int getStorageDemand(){
        return storageDemand;
    }
    String getServiceId(){
        return serviceId;
    }

    void setServiceTransTime(double addTime) { this.transTimeServices += addTime;};

    long getServiceBirthTime() { return serviceBirthTime ;}

    double getServiceTransTime() {
        return transTimeServices;
    }

    int getPacketSize(){
        return packetSize;
    }

    /*public List<Packet> getPackets(){
        return this.servicePackets;
    }*/

    Services(int compDemand ,int strgDemand,int size){ // constructing the service with given demand, storage and size.
        this.computationDemand = compDemand;
        this.storageDemand = strgDemand;
        this.packetSize = size;
        this.transTimeServices = 0;
        serviceBirthTime = System.currentTimeMillis();
        this.serviceId = UUID.randomUUID().toString();
        for(int i=0;i<strgDemand;i++){
            Packet packet = new Packet();
            servicePackets.add(packet);
        }
    }
}
