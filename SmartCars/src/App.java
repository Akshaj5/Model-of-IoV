import com.sun.istack.internal.Nullable;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class App implements Runnable {
    
    //When all to find the positions of the vehicles and update them ? - DONE
    //Change speed of carSystem.out.println(sumOfDelays/deliPackets);
    //create random packets in cars at certain time
    //Get all the time from the List and make the avg delay - DONE
    //keep a count on the no of packets generated and delivered - DONE
    //if packet time is beyond 150s , drop it

    private static List< RSU[] > towers = new ArrayList<>();
    private static int noOfCars = 300;
    private static List<Vehicle> cars = new ArrayList<>();
    private volatile static boolean exit = false;
    private static double finalDeliveryDelay = 0;
    private static int totalMigration;
    private static double sumOfDelays;
    private static int bufferSize =8;
    private static int deliServices;
    private static int deliPacketsToRsu;
    private static int deliPacketsToCars;
    private Lock carsLock = new ReentrantLock();

    static class FixedLengthQueueServices{

        private int size = bufferSize;
        private Queue<Services> servicesQueue = new LinkedList<>();

        boolean checkIfSpace() {
            return servicesQueue.size() < size;
        }
    }

    static class FixedLengthQueueStrings{
        private Queue<String> queue = new LinkedList<>();
    }


    static class Buffer implements Runnable {

        //private Queue<Services> servicesQueue = new LinkedList<>();
        FixedLengthQueueServices services = new FixedLengthQueueServices();
        FixedLengthQueueStrings carIds = new FixedLengthQueueStrings();
        //private Queue<String> carIds = new LinkedList<>();
        private List<Vehicle> list1 = new ArrayList<>();
        private List<Vehicle> list2 = new ArrayList<>();

        private void addService(Services service){
            this.services.servicesQueue.add(service);
        }
        private void addId(String id){
            this.carIds.queue.add(id);
        }
        private void setList1(List<Vehicle> list){this.list1 =list;}
        private void setList2(List<Vehicle> list){this.list2 =list;}
        public void run(){
            while(!exit){
                if(services.servicesQueue.size()!=0){
                    int flag =0;
                    int comp = services.servicesQueue.element().getComputationDemand();

                    int time = services.servicesQueue.element().getPacketSize()*services.servicesQueue.element().getStorageDemand()*8/comp;

                    try {
                        Thread.sleep(time);

                        //for(int i=0;i<list1.size();i++){
                        for(Vehicle car : list1){
                            if(car.getId().equals(carIds.queue.element())){
                                flag =1;
                                carIds.queue.remove();
                                Services selectedService = services.servicesQueue.element();
                                //int packets = selectedService.getStorageDemand();
                                //double transTime = selectedService.getPacketSize()*packets*8/6;
                                //selectedService.setServiceTransTime(transTime);
                                long currentTime = System.currentTimeMillis();
                                long birthTime = selectedService.getServiceBirthTime();
                                double travelTime = selectedService.getServiceTransTime();
                                finalDeliveryDelay += currentTime - birthTime + travelTime + time;
                                deliPacketsToCars += selectedService.getStorageDemand();
                                services.servicesQueue.remove();
                                deliServices += 1;
                            }
                        }
                        for(Vehicle car : list2){
                            if(car.getId().equals(carIds.queue.element())){
                                flag =1;
                                carIds.queue.remove();
                                Services selectedService = services.servicesQueue.element();
                                //int packets = selectedService.getStorageDemand();
                                //double transTime = selectedService.getPacketSize()*packets*8/6;
                                //selectedService.setServiceTransTime(transTime);
                                long currentTime = System.currentTimeMillis();
                                long birthTime = selectedService.getServiceBirthTime();
                                double travelTime = selectedService.getServiceTransTime();
                                finalDeliveryDelay += currentTime - birthTime + travelTime + time;
                                deliPacketsToCars += selectedService.getStorageDemand();
                                services.servicesQueue.remove();
                                deliServices +=1;
                            }
                        }
                        if(flag==0){
                            carIds.queue.remove();
                            totalMigration++;
                            int demand = services.servicesQueue.element().getComputationDemand();
                            int time1 = services.servicesQueue.element().getPacketSize()*services.servicesQueue.element().getStorageDemand()*8/demand;
                            Services selectedService = services.servicesQueue.element();
                            //int packets = selectedService.getStorageDemand();
                            //double transTime = selectedService.getPacketSize()*packets*8/6;
                            //selectedService.setServiceTransTime(transTime);
                            long currentTime = System.currentTimeMillis();
                            long birthTime = selectedService.getServiceBirthTime();
                            double travelTime = selectedService.getServiceTransTime();
                            finalDeliveryDelay += currentTime - birthTime + travelTime + time1 + time;
                            deliPacketsToCars += selectedService.getStorageDemand();
                            services.servicesQueue.remove();
                            deliServices +=1;
                        }
                    }catch(Exception e){
                       // return;
                    }
                }
            }
        }
    }

    private double distanceCT(Vehicle car, RSU tower) {
        float carX = car.getPosition().getX(), carY = car.getPosition().getY();
        float towerX = tower.getPosition().getX(), towerY = tower.getPosition().getY();
        //return Math.sqrt(Math.pow(carX - towerX, 2) + Math.pow(carY - towerY, 2));
        return carX - towerX + carY - towerY;
    }

    private double distanceCC(Vehicle car1, Vehicle car2) {
        float car1X = car1.getPosition().getX(), car1Y = car1.getPosition().getY();
        float car2X = car2.getPosition().getX(), car2Y = car2.getPosition().getY();
        return Math.sqrt(Math.pow(car1X - car2X, 2) + Math.pow(car1Y - car2Y, 2));
    }

    private boolean isWithinRangeCT(Vehicle car, RSU tower,int roadId,int towerId){
        if(((car.getDirection() == -1 && towerId%2 ==1)&&(roadId ==0||roadId==1)) ||((car.getDirection() == 1 && towerId%2 ==0)&&(roadId==2||roadId==3))){
            return -900<=distanceCT(car,tower) && distanceCT(car, tower) <= 750;
        }
        else if(((car.getDirection() ==-1 &&towerId%2 ==1)&&(roadId==2||roadId==3))||((car.getDirection() == 1 && towerId%2 ==0)&&(roadId==0||roadId==1))){
            return -750<=distanceCT(car,tower) && distanceCT(car, tower) <= 900;
        }
        else{
            return -900<=distanceCT(car,tower) && distanceCT(car,tower)<=900;
        }
    }

    private boolean isWithinRangeCC(Vehicle car1, Vehicle car2) {
        return distanceCC(car1, car2) <= 300;
    }

    private int neighborDirection(int id, Vehicle car, RSU tower) {
        int temp = 0;
        if(id == 0) {
            if(car.getPosition().getX() < tower.getPosition().getX())
                temp =  -1;
            else
                temp =  1;
        }
        if(id == 1) {
            if(car.getPosition().getY() < tower.getPosition().getY())
                temp =  -1;
            else
                temp =  1;

        }
        if(id == 2) {
            if(car.getPosition().getX() < tower.getPosition().getX())
                temp = 1;
            else
                temp = -1;
        }
        if(id == 3) {
            if(car.getPosition().getY() < tower.getPosition().getY())
                temp = 1;
            else
                temp = -1;
        }
        return temp;
    }

    private void changeCarPosition() {
        for(int i = 0; i < noOfCars; i++) {
            Vehicle car = cars.get(i);
            long currentTime = System.currentTimeMillis();
            long changeTime = currentTime - car.getPositionSetTime();
            int id = car.getRoadId();
            cars.get(i).setPositionSetTime(currentTime);
            float distance = ((car.getSpeed())/3600) * changeTime;
            if(id == 0) {
                float newPositionPositive = car.getPosition().getX() + distance;    //the distance the car is at when we want to update if in positive direction along its road
                float newPositionNegative = car.getPosition().getX() - distance;
                if(car.getDirection() == -1) {
                    if(newPositionPositive >= 15000) {
                        cars.get(i).setPosition(new Position(15000, newPositionPositive - 15000));
                        cars.get(i).setRoadId(1);
                    }
                    else {
                        cars.get(i).setPosition(new Position(newPositionPositive, 0));
                    }
                }
                else {
                    if(newPositionNegative < 0) {
                        cars.get(i).setPosition(new Position(0,  -newPositionNegative));
                        cars.get(i).setRoadId(3);
                    }
                    else {
                        cars.get(i).setPosition(new Position(newPositionNegative, 0));
                    }
                }
            }

            else if(id == 1) {
                float newPositionPositive = car.getPosition().getY() + distance;
                float newPositionNegative = car.getPosition().getY() - distance;
                if(car.getDirection() == -1) {
                    if(newPositionPositive >= 5000) {
                        cars.get(i).setPosition(new Position(15000 - (newPositionPositive - 5000), 5000));
                        cars.get(i).setRoadId(2);
                    }
                    else {
                        cars.get(i).setPosition((new Position(15000, newPositionPositive)));
                    }
                }
                else {
                    if(newPositionNegative < 0) {
                        cars.get(i).setPosition(new Position(15000 + newPositionNegative, 0));
                        cars.get(i).setRoadId(0);
                    }
                    else {
                        cars.get(i).setPosition(new Position(150000, newPositionNegative));
                    }
                }
            }

            else if(id == 2) {
                float newPositionPositive = car.getPosition().getX() + distance;
                float newPositionNegative = car.getPosition().getX() - distance;
                if(car.getDirection() == -1) {
                    if(newPositionNegative < 0) {
                        cars.get(i).setPosition(new Position(0, 5000 + newPositionNegative));
                        cars.get(i).setRoadId(3);
                    }
                    else {
                        cars.get(i).setPosition(new Position(newPositionNegative, 5000));
                    }
                }
                else {
                    if(newPositionPositive >= 15000) {
                        cars.get(i).setPosition(new Position(15000, 5000 - (newPositionPositive - 15000)));
                        cars.get(i).setRoadId(1);
                    }
                    else {
                        cars.get(i).setPosition(new Position(newPositionPositive, 5000));
                    }
                }
            }

            else if(id == 3) {
                float newPositionPositive = car.getPosition().getY() + distance;
                float newPositionNegative = car.getPosition().getY() - distance;
                if(car.getDirection() == -1) {
                    if(newPositionNegative < 0) {
                        cars.get(i).setPosition(new Position(-newPositionNegative, 0));
                        cars.get(i).setRoadId(0);
                    }
                    else {
                        cars.get(i).setPosition(new Position(newPositionNegative, 0));
                    }
                }
                else {
                    if(newPositionPositive >= 5000) {
                        cars.get(i).setPosition(new Position(newPositionPositive - 5000, 5000));
                        cars.get(i).setRoadId(2);
                    }
                    else {
                        cars.get(i).setPosition(new Position(newPositionPositive, 0));
                    }
                }
            }
        }
    }

    public void run() {
        // 1. Select the car.
        // 2. Find the time to finish everything.
        // 3. Sleep for that much time.
        // 4. Go to 1.
        Thread t = Thread.currentThread();
        String[] s = t.getName().split(",");
        int roadId = Integer.parseInt(s[0]), towerId = Integer.parseInt(s[1]);
        RSU tower = towers.get(roadId)[towerId];
        List<Vehicle> carsWithinRange = new ArrayList<>();
        List<Vehicle> broadcastCars = new ArrayList<>();
        List<Vehicle> carsHavingServices = new ArrayList<>();

        while(!exit) {
            try {
                changeCarPosition();
            } catch(Exception ex) {
                //return;
            }
            carsWithinRange.clear();
            carsHavingServices.clear();
            for (int i = 0; i < noOfCars; i++) {
                try {
                    Vehicle car = cars.get(i);
                    if (car.getRoadId() == Integer.parseInt(s[0]) && isWithinRangeCT(car, tower,roadId,towerId)) {
                        carsWithinRange.add(car);
                        if(car.ifServiceQueueNotEmpty()) {
                            carsHavingServices.add(car);
                        }
                    }
                } catch(Exception ex) {
                    //return;
                }
            }
            int totalCarsWithinRange = carsWithinRange.size();
            long max = 0;

            Vehicle selectedCar = null;
            try {
                for (Vehicle temp : carsHavingServices) {
                    long startTime = temp.getNextService().getServiceBirthTime();
                    long currentTime = System.currentTimeMillis();
                    if((currentTime-startTime)>max){
                        max = currentTime-startTime;
                        selectedCar = temp;
                    }
                }

                if (totalCarsWithinRange == 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        //return;
                    }
                } else {
                    @Nullable Lock lock = selectedCar.getLock();
                    if (lock.tryLock()) {
                        try {
                            //If MultiHop
                            if(Math.abs(distanceCT(selectedCar, tower)) > 300) {
                                broadcastCars.clear();
                                changeCarPosition();
                                // selecting cars which are in the range of the selected car and in the direction of that tower.
                                for (int i = 0; i < noOfCars; i++) {
                                    Vehicle candidate = cars.get(i);
                                    if (isWithinRangeCC(selectedCar, candidate)) {
                                        if (Math.abs(distanceCT(candidate, tower)) < Math.abs(distanceCT(selectedCar, tower))) {
                                            //choose cars which go towards the tower
                                            int dir = neighborDirection(Integer.parseInt(s[0]), selectedCar, tower);
                                            if(candidate.getDirection() == dir) {
                                                broadcastCars.add(candidate);
                                            }
                                        }
                                    }
                                }
                                // broadcasting the service to other cars because it is not in the direct range of the tower.
                                if (selectedCar.ifServiceQueueNotEmpty()) {
                                    Services selectedService = selectedCar.getNextService();
                                    selectedCar.popService(selectedService);
                                    double transTime = selectedService.getPacketSize()*selectedService.getStorageDemand()*8/6;
                                    selectedService.setServiceTransTime(transTime);
                                    //for (int i = 0; i < broadcastCars.size(); i++) {
                                    for(Vehicle car : broadcastCars){
                                        for (int j = 0; j < noOfCars; j++) {
                                            if(cars.get(j).getId().equals(car.getId())){
                                                cars.get(j).pushService(selectedService);
                                            }
                                        }
                                    }
                                }
                            }
                            //If no MultiHop
                            else{
                                if(selectedCar.ifServiceQueueNotEmpty()) {

                                    Services selectedService = selectedCar.getNextService();

                                    long deathTime = System.currentTimeMillis();
                                    int packets = selectedService.getStorageDemand();
                                    double transTime = selectedService.getPacketSize()*packets*8/6;
                                    selectedService.setServiceTransTime(transTime);
                                    sumOfDelays += deathTime - selectedService.getServiceBirthTime() + selectedService.getServiceTransTime();
                                    deliPacketsToRsu += selectedService.getStorageDemand();

                                    if(towers.get(roadId)[towerId].getBuffer().services.checkIfSpace()) {
                                        towers.get(roadId)[towerId].getBuffer().addService(selectedService);
                                        towers.get(roadId)[towerId].getBuffer().addId(selectedCar.getId());
                                    }
                                    if(towerId%2==0){
                                        towers.get(roadId)[towerId].getBuffer().setList1(carsWithinRange);
                                    }
                                    else{
                                        towers.get(roadId)[towerId].getBuffer().setList2(carsWithinRange);
                                    }
                                    while(!carsLock.tryLock());
                                    try {
                                        for (int j = 0; j < noOfCars; j++) { // removing that service from all the cars because its delivered now.
                                            Queue<Services> servicesQueue = cars.get(j).getServicesQueue();       //From here is the code to iterate a queue
                                            for (Services item : servicesQueue) {
                                                if (item.getServiceId().equals(selectedService.getServiceId())) {
                                                    cars.get(j).popService(item);// remove that service.
                                                    break;
                                                }
                                            }
                                        }
                                    } finally {
                                        carsLock.unlock();
                                    }
                                }
                            }

                        } catch(Exception e) {
                            //return;
                        }
                        finally {
                            lock.unlock();
                        }
                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            //return;
                        }
                    }
                }
            }catch (NullPointerException | NoSuchElementException e){
               // return;
            }

        }
    }

    private static void stop() {
        exit = true;
    }


    public static void main(String[] args) {


            int p1 = 900, p2 = 900, p3 = 14100, p4 = 4500;
            for (int i = 0; i < 4; i++) { // Placing the towers on the roads.
                if (i % 2 == 0) {
                    towers.add(new RSU[12]);
                } else {
                    towers.add(new RSU[4]);
                }
                for (int j = 0; j < towers.get(i).length; j++) {
                    Buffer buff;
                    if ((j) % 2 == 0) {
                        buff = new App.Buffer();
                    } else {
                        buff = towers.get(i)[j - 1].getBuffer();
                    }
                    if (i == 0) {
                        towers.get(i)[j] = new RSU(p1, 0, buff);
                        p1 += 1200;
                    } else if (i == 1) {
                        towers.get(i)[j] = new RSU(15000, p2, buff);
                        p2 += 1200;
                    } else if (i == 2) {
                        towers.get(i)[j] = new RSU(p3, 5000, buff);
                        p3 -= 1200;
                    } else if (i == 3) {
                        towers.get(i)[j] = new RSU(0, p4, buff);
                        p4 -= 1200;
                    }
                }
            }
            int totalServices;
            //col =0;
            noOfCars = 100;
            for (int k = 1; k <= 5; k++,noOfCars+=100) {
                cars.clear();
                deliPacketsToRsu = 0;
                deliPacketsToCars = 0;
                deliServices = 0;
                totalServices = 0;
                sumOfDelays = 0;
                totalMigration = 0;
                finalDeliveryDelay = 0;

                for (int i = 0; i < noOfCars; i++) { // filling the cars in an array i.e. buffer.
                    Random rand = new Random();
                    int noOfPackets = rand.nextInt(4) + 2;
                    int computationDemand = rand.nextInt(60) + 30;
                    int size = rand.nextInt(1000) + 10;
                    Services service = new Services(computationDemand, noOfPackets, size); // Initialising Services for each vehicle.
                    Vehicle vehicle = new Vehicle(service);
                    cars.add(vehicle);
                    totalServices += 1;
                }
                exit = false;
                for (Integer i = 0; i < 4; i++) {
                    for (Integer j = 0; j < towers.get(i).length; j++) {
                        if (j % 2 == 0) {
                            Thread th = new Thread(towers.get(i)[j].getBuffer());
                            th.start();
                        }
                        Thread t = (new Thread(new App())); // Starting all towers simultaneously.
                        t.setName(i.toString() + "," + j.toString());
                        t.start();
                    }
                }
                try {
                    Thread.sleep(5000);
                    for (int i = 0; i < 11; i++) {
                        for (int j = 0; j < noOfCars; j++) {
                            //for(int l=1;l<=k;l++) {
                                Random rand = new Random();
                                int noOfCompUnits = rand.nextInt(4) + 2;
                                int computationDemand = rand.nextInt(60) + 30;
                                int size = rand.nextInt(1000) + 10;
                                Services service = new Services(computationDemand, noOfCompUnits, size);
                                cars.get(j).pushService(service);
                                totalServices += 1;
                            //}
                        }
                        Thread.sleep(5000);
                    }
                    stop();

                } catch (InterruptedException e) {
                    // return;
                }

                System.out.println("Packet Delay time:" + String.valueOf((finalDeliveryDelay / deliPacketsToCars) + (sumOfDelays / deliPacketsToRsu))); // no of delivered packets.

                System.out.println("Total Migration:" + String.valueOf(totalMigration)); // no of delivered packets.

                System.out.println("Packet Delivery Ratio:" + String.valueOf((float) deliServices / (float) totalServices)); // no of delivered packets.

                System.out.println("Delivered Services:" + String.valueOf(deliServices)); // no of delivered packets.

                System.out.println("Total Services:" + String.valueOf(totalServices)); // no of delivered packets.

                System.out.println("No of cars:" + String.valueOf(noOfCars));// total no. of cars in that particular iteration.
                deliServices = 0;
            }

    }
}