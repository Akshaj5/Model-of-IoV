import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Vehicle {

    private String id;
    private int speed;
    private int direction;
    private int roadId;
    private Position position;
    private Queue<Services> servicesQueue = new LinkedList<>();
    private long positionSetTime;
    private Lock lock;

    public Lock getLock() {
        return lock;
    }

    public boolean ifServiceQueueNotEmpty() {
        if(servicesQueue.size() != 0)
            return true;
        return false;
    }

    public Services getNextService() {
        return servicesQueue.element();
    }

    public void pushService(Services service) {
        servicesQueue.add(service);
    }

    public void popService(Services service) {
        servicesQueue.remove(service);
    }
    public void removeService() {servicesQueue.remove();}


    public String getId() {
        return id;
    }

    public int getSpeed() {
        return speed;
    }

    public int getDirection() {
        return direction;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public int getRoadId() {
        return roadId;
    }

    public void setRoadId(int roadId) {
        this.roadId = roadId;
    }

    public long getPositionSetTime(){
        return positionSetTime;
    }

    public void setPositionSetTime(long num){
        this.positionSetTime = num;
    }

    public Queue<Services> getServicesQueue() {
        return servicesQueue ;
    }

    public Vehicle(Services service) { // making a vehicle with all the parameters.
        Random rand = new Random();
        this.id = UUID.randomUUID().toString();
        this.speed = (int)(Math.random() * 60) + 30;
        this.direction = rand.nextInt(2) * 2 - 1;
        this.roadId = rand.nextInt(4);
        //this.service = service;
        this.servicesQueue.add(service);
        position = new Position(this.roadId);
        lock = new ReentrantLock();
    }
}