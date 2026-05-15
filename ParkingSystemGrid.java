import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.Queue;

public class ParkingSystemGrid extends JFrame {

    private final int TOTAL_SLOTS = 10;
    private JButton[] slotButtons;
    private JLabel statusLabel;
    private JLabel waitingListLabel;
    private JButton parkButton; 

    private ParkingLot parkingLot;

    public ParkingSystemGrid() {
        
        parkingLot = new ParkingLot(TOTAL_SLOTS);

        setTitle("Smart Parking System");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Smart Parking System");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(2, 5, 10, 10)); 
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        slotButtons = new JButton[TOTAL_SLOTS];
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            final int slotIndex = i; 
            
            JButton button = new JButton("Slot " + (i + 1));
            button.setFont(new Font("Arial", Font.BOLD, 14));
            
           
            button.addActionListener(e -> onSlotClick(slotIndex));
            
            slotButtons[i] = button;
            gridPanel.add(button);
        }
        add(gridPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Welcome!");
        waitingListLabel = new JLabel("Waiting: 0  ");

        parkButton = new JButton("Park Vehicle");
        parkButton.setBackground(Color.CYAN);
        parkButton.addActionListener(e -> handleParkOrQueue());

        controlPanel.add(parkButton, BorderLayout.WEST); 
        controlPanel.add(statusLabel, BorderLayout.CENTER);
        controlPanel.add(waitingListLabel, BorderLayout.EAST);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(controlPanel, BorderLayout.SOUTH);

        updateAllButtons();
        setLocationRelativeTo(null); 
        setVisible(true);
    }
    private void onSlotClick(int slotIndex) {
        if (parkingLot.isSlotOccupied(slotIndex)) {
            handleRemoval(slotIndex);
        }
    }
  
    private void handleParkOrQueue() {
        String number = JOptionPane.showInputDialog(this, "Enter Vehicle Number:", "Park Vehicle", JOptionPane.PLAIN_MESSAGE);
        if (number == null || number.trim().isEmpty()) {
            return; 
        }
        String[] types = {"Car", "Motorcycle"};
        String type = (String) JOptionPane.showInputDialog(this, 
                "Select Vehicle Type:", "Vehicle Type", 
                JOptionPane.QUESTION_MESSAGE, null, types, types[0]);
        if (type == null) {
            return; 
        }

        Vehicle vehicle = (type.equals("Car")) ? new Car(number) : new Motorcycle(number);

        try {
            String resultMessage = parkingLot.parkOrQueue(vehicle);
            statusLabel.setText(resultMessage);
        } catch (VehicleAlreadyParkedException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Parking Error", JOptionPane.WARNING_MESSAGE);
        }
        
        updateAllButtons();
    }

    private void handleRemoval(int slotIndex) {
        try {
            Vehicle v = parkingLot.getVehicleInSlot(slotIndex);
            String message = "Vehicle: " + v.getNumber() + " (" + v.getType() + ")\nDo you want to remove this vehicle?";
            
            int choice = JOptionPane.showConfirmDialog(this, message, "Remove Vehicle?", JOptionPane.YES_NO_OPTION);
            
            if (choice == JOptionPane.YES_OPTION) {
                Vehicle nextVehicle = parkingLot.removeVehicle(slotIndex);
                
                String status = "Success: Vehicle " + v.getNumber() + " removed from Slot " + (slotIndex + 1);
 
                if (nextVehicle != null) {
                    status += ". Vehicle " + nextVehicle.getNumber() + " from waiting list parked.";
                }
                statusLabel.setText(status);
            }
        } catch (SlotEmptyException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Removal Error", JOptionPane.ERROR_MESSAGE);
        }

        updateAllButtons();
    }
    private void updateAllButtons() {
                int occupiedCount = 0;
                 for (int i = 0; i < TOTAL_SLOTS; i++) {
 try {
 if (parkingLot.isSlotOccupied(i)) {
                            Vehicle v = parkingLot.getVehicleInSlot(i);
                            slotButtons[i].setText("<html><center><b>" + v.getNumber() + "</b><br>(" + v.getType() + ")</center></html>");
                            slotButtons[i].setBackground(new Color(255, 100, 100)); // Red
                            slotButtons[i].setForeground(Color.WHITE); // white text on red
                            slotButtons[i].setEnabled(true);
                            occupiedCount++;
                } else {
                            slotButtons[i].setText("Slot " + (i + 1) + " (Available)");
                            slotButtons[i].setBackground(new Color(0, 180, 0));// dark green
                            slotButtons[i].setForeground(Color.BLACK);  // black text
                            slotButtons[i].setEnabled(true); // <-- This was changed from false
            }


 }

catch (SlotEmptyException e) {
 slotButtons[i].setText("Error");
 slotButtons[i].setBackground(Color.GRAY);
 slotButtons[i].setForeground(Color.BLACK);
slotButtons[i].setEnabled(false);
 }
 }
 
statusLabel.setText("Available Slots: " + (TOTAL_SLOTS - occupiedCount) + " / " + TOTAL_SLOTS);
 waitingListLabel.setText("Waiting: " + parkingLot.getWaitingListSize() + "  ");
 }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ParkingSystemGrid());
    }
}

class ParkingLot {

    private ParkingSlot[] slots;

    private Queue<Vehicle> waitingQueue;

    public ParkingLot(int size) {
        slots = new ParkingSlot[size];
        for (int i = 0; i < size; i++) {
            slots[i] = new ParkingSlot(i + 1);
        }
        waitingQueue = new LinkedList<>(); 
    }

    public boolean isSlotOccupied(int slotIndex) {
        return slots[slotIndex].isOccupied();
    }
    
    public int getWaitingListSize() {
        return waitingQueue.size();
    }

    public Vehicle getVehicleInSlot(int slotIndex) throws SlotEmptyException {
        if (!isSlotOccupied(slotIndex)) {
            throw new SlotEmptyException("Slot " + (slotIndex + 1) + " is already empty.");
        }
        return slots[slotIndex].getVehicle();
    }

    public String parkOrQueue(Vehicle vehicle) throws VehicleAlreadyParkedException {
        for (ParkingSlot slot : slots) {
            if (slot.isOccupied() && slot.getVehicle().getNumber().equals(vehicle.getNumber())) {
                throw new VehicleAlreadyParkedException("Vehicle " + vehicle.getNumber() + " is already parked in Slot " + slot.getSlotNumber());
            }
        }

        for (int i = 0; i < slots.length; i++) {
            if (!slots[i].isOccupied()) {
                slots[i].park(vehicle);
                return "Success: Vehicle " + vehicle.getNumber() + " parked in Slot " + (i + 1);
            }
        }

        waitingQueue.add(vehicle);
        return "Lot is full. Vehicle " + vehicle.getNumber() + " added to waiting list.";
    }

    public Vehicle removeVehicle(int slotIndex) throws SlotEmptyException {
        if (!isSlotOccupied(slotIndex)) {
            throw new SlotEmptyException("Slot " + (slotIndex + 1) + " is already empty.");
        }
        
        slots[slotIndex].vacate();
  
        if (!waitingQueue.isEmpty()) {
            Vehicle nextVehicle = waitingQueue.poll(); 
            slots[slotIndex].park(nextVehicle); 
            return nextVehicle; 
        }
        
        return null; 
    }
}

abstract class Vehicle {
    private String number;
    public Vehicle(String number) { this.number = number; }
    public String getNumber() { return number; }
    public abstract String getType();
}

class Car extends Vehicle {
    public Car(String number) { super(number); }
    @Override
    public String getType() { return "Car"; }
}

class Motorcycle extends Vehicle {
    public Motorcycle(String number) { super(number); }
    @Override
    public String getType() { return "Motorcycle"; }
}

class ParkingSlot {
    private int slotNumber;
    private Vehicle vehicle;
    private boolean isOccupied;

    public ParkingSlot(int slotNumber) {
        this.slotNumber = slotNumber;
        this.isOccupied = false;
        this.vehicle = null;
    }

    public void park(Vehicle v) {
        this.vehicle = v;
        this.isOccupied = true;
    }

    public void vacate() {
        this.vehicle = null;
        this.isOccupied = false;
    }

    public boolean isOccupied() { return isOccupied; }
    public Vehicle getVehicle() { return vehicle; }
    public int getSlotNumber() { return slotNumber; }
}

class VehicleAlreadyParkedException extends Exception {
    public VehicleAlreadyParkedException(String message) {
        super(message);
    }
}
class SlotEmptyException extends Exception {
    public SlotEmptyException(String message) {
        super(message);
    }
}