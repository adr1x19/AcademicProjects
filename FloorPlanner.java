import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

public class FloorPlanApp extends JFrame {
    private JPanel floorPlanPanel;
    private ArrayList<RoomPanel> rooms;
    private static final int GRID_SPACING = 20;
    private boolean isGridVisible = true;
    private RoomPanel selectedRoom = null;

    public FloorPlanApp() {
        rooms = new ArrayList<>();

        setTitle("2D Floor Planner");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topNavBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topNavBar.setBackground(Color.DARK_GRAY);

        JButton projectButton = new JButton("Assign Project");
        JButton clearButton = new JButton("Clear");
        JButton saveButton = new JButton("Save");
        JButton openButton = new JButton("Open Project");

        JLabel titleLabel = new JLabel("ALGORITHIM AVENGERS");
        titleLabel.setForeground(Color.WHITE);

        topNavBar.add(projectButton);
        topNavBar.add(clearButton);
        topNavBar.add(saveButton);
        topNavBar.add(openButton);
        topNavBar.add(Box.createHorizontalStrut(150));
        topNavBar.add(titleLabel);

        add(topNavBar, BorderLayout.NORTH);

        JPanel sidePanel = new JPanel();
        sidePanel.setBackground(Color.LIGHT_GRAY);
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setPreferredSize(new Dimension(200, 700));

        JButton placeRoomButton = new JButton("Add Room");
        JButton toggleGridButton = new JButton("Show Grid");
        JButton addDoorButton = new JButton("Add Door");
        JButton addWindowButton = new JButton("Add Window");
        JButton deleteRoomButton = new JButton("Delete Room");
        JButton addFurnitureButton = new JButton("Add Furniture");

        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(placeRoomButton);
        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(toggleGridButton);
        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(addDoorButton);
        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(addWindowButton);
        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(deleteRoomButton);
        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(addFurnitureButton);

        add(sidePanel, BorderLayout.WEST);

        floorPlanPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                if (isGridVisible) {
                    g2d.setColor(new Color(0, 0, 128, 64));

                    g2d.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0));
                    for (int x = 0; x < getWidth(); x += GRID_SPACING) {
                        g2d.drawLine(x, 0, x, getHeight());
                    }
                    for (int y = 0; y < getHeight(); y += GRID_SPACING) {
                        g2d.drawLine(0, y, getWidth(), y);
                    }
                }
            }
        };
        floorPlanPanel.setBackground(Color.WHITE);
        floorPlanPanel.setLayout(null);
        floorPlanPanel.setDoubleBuffered(true);
        add(floorPlanPanel, BorderLayout.CENTER);

        projectButton.addActionListener(e -> createNewProject());
        clearButton.addActionListener(e -> clearRooms());
        saveButton.addActionListener(e -> saveToFile());
        openButton.addActionListener(e -> openFromFile());
        placeRoomButton.addActionListener(e -> addRoomDialog());
        toggleGridButton.addActionListener(e -> {
            isGridVisible = !isGridVisible;
            toggleGridButton.setText(isGridVisible ? "Hide Grid" : "Show Grid");
            floorPlanPanel.repaint();
        });
        addDoorButton.addActionListener(e -> addDoorDialog());
        addWindowButton.addActionListener(e -> addWindowDialog());
        deleteRoomButton.addActionListener(e -> deleteSelectedRoom());
        addFurnitureButton.addActionListener(e -> addFurnitureDialog());

        setVisible(true);
    }

    private void createNewProject() {
        clearRooms();
        JOptionPane.showMessageDialog(this, "New project created.", "New Project", JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveToFile() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(file)) {
                for (RoomPanel room : rooms) {
                    writer.write(room.roomName + "," + room.getX() + "," + room.getY() + "," + room.getWidth() + "," + room.getHeight() + "," + room.getBackground().getRGB() + "\n");
                }
                JOptionPane.showMessageDialog(this, "File saved successfully.", "Save", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                clearRooms();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 6) {
                        String name = parts[0];
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        int width = Integer.parseInt(parts[3]);
                        int height = Integer.parseInt(parts[4]);
                        Color color = new Color(Integer.parseInt(parts[5]));
                        RoomPanel room = new RoomPanel(name, x, y, width, height, color);
                        createRoom(room);
                    }
                }
                JOptionPane.showMessageDialog(this, "Project opened successfully.", "Open Project", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException | NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Error opening file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearRooms() {
        for (RoomPanel room : rooms) {
            floorPlanPanel.remove(room);
        }
        rooms.clear();
        floorPlanPanel.revalidate();
        floorPlanPanel.repaint();
    }

    private void addRoomDialog() {
        if (rooms.isEmpty()) {
            originalAddRoomDialog();
            return;
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField(20);
        JSpinner widthField = new JSpinner(new SpinnerNumberModel(100, 20, 500, 10));
        JSpinner heightField = new JSpinner(new SpinnerNumberModel(100, 20, 500, 10));

        String[] roomTypes = {"Bedroom", "Bathroom", "Kitchen", "Living Room"};
        JComboBox<String> roomTypeBox = new JComboBox<>(roomTypes);

        String[] colorNames = {"Red", "Blue", "Green", "Cyan", "Magenta", "Yellow", "Orange"};
        JComboBox<String> colorBox = new JComboBox<>(colorNames);
        colorBox.setRenderer(new ColorCellRenderer());

        JComboBox<RoomPanel> existingRoomsBox = new JComboBox<>(rooms.toArray(new RoomPanel[0]));
        existingRoomsBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                if (value instanceof RoomPanel) {
                    RoomPanel room = (RoomPanel) value;
                    return super.getListCellRendererComponent(list, room.roomName,
                            index, isSelected, cellHasFocus);
                }
                return super.getListCellRendererComponent(list, value,
                        index, isSelected, cellHasFocus);
            }
        });

        String[] positionOptions = {"North", "South", "East", "West"};
        JComboBox<String> positionBox = new JComboBox<>(positionOptions);

        panel.add(new JLabel("Room Type:"));
        panel.add(roomTypeBox);
        panel.add(new JLabel("Room Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Existing Room:"));
        panel.add(existingRoomsBox);
        panel.add(new JLabel("Position Relative To Room:"));
        panel.add(positionBox);
        panel.add(new JLabel("Width:"));
        panel.add(widthField);
        panel.add(new JLabel("Height:"));
        panel.add(heightField);
        panel.add(new JLabel("Color:"));
        panel.add(colorBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Room",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String roomType = (String) roomTypeBox.getSelectedItem();
                String name = nameField.getText().trim();
                RoomPanel baseRoom = (RoomPanel) existingRoomsBox.getSelectedItem();
                String position = (String) positionBox.getSelectedItem();
                int width = ((Number) widthField.getValue()).intValue();
                int height = ((Number) heightField.getValue()).intValue();
                Color color = getColorFromName((String) colorBox.getSelectedItem());

                int newX = baseRoom.getX();
                int newY = baseRoom.getY();

                switch (position) {
                    case "North":
                        newY = baseRoom.getY() - height - GRID_SPACING;
                        newX = baseRoom.getX();
                        break;
                    case "South":
                        newY = baseRoom.getY() + baseRoom.getHeight() + GRID_SPACING;
                        newX = baseRoom.getX();
                        break;
                    case "East":
                        newX = baseRoom.getX() + baseRoom.getWidth() + GRID_SPACING;
                        newY = baseRoom.getY();
                        break;
                    case "West":
                        newX = baseRoom.getX() - width - GRID_SPACING;
                        newY = baseRoom.getY();
                        break;
                }

                RoomPanel newRoom = new RoomPanel(name + " (" + roomType + ")", newX, newY, width, height, color);
                if (checkRoomOverlap(newRoom)) {
                    JOptionPane.showMessageDialog(this, "Room overlaps with another room. Please adjust the position.", "Overlap Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    createRoom(newRoom);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Invalid input values",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void originalAddRoomDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField(20);
        JSpinner xField = new JSpinner(new SpinnerNumberModel(10, 0, 100, 1));
        JSpinner yField = new JSpinner(new SpinnerNumberModel(10, 0, 100, 1));
        JSpinner widthField = new JSpinner(new SpinnerNumberModel(100, 20, 500, 10));
        JSpinner heightField = new JSpinner(new SpinnerNumberModel(100, 20, 500, 10));

        String[] colorNames = {"Red", "Blue", "Green", "Cyan", "Magenta", "Yellow", "Orange"};
        JComboBox<String> colorBox = new JComboBox<>(colorNames);
        colorBox.setRenderer(new ColorCellRenderer());

        panel.add(new JLabel("Room Name:"));
        panel.add(nameField);
        panel.add(new JLabel("X Position (Grid):"));
        panel.add(xField);
        panel.add(new JLabel("Y Position (Grid):"));
        panel.add(yField);
        panel.add(new JLabel("Width:"));
        panel.add(widthField);
        panel.add(new JLabel("Height:"));
        panel.add(heightField);
        panel.add(new JLabel("Color:"));
        panel.add(colorBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Room",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                int x = ((Number) xField.getValue()).intValue() * GRID_SPACING;
                int y = ((Number) yField.getValue()).intValue() * GRID_SPACING;
                int width = ((Number) widthField.getValue()).intValue();
                int height = ((Number) heightField.getValue()).intValue();
                Color color = getColorFromName((String) colorBox.getSelectedItem());

                RoomPanel newRoom = new RoomPanel(name, x, y, width, height, color);
                if (checkRoomOverlap(newRoom)) {
                    JOptionPane.showMessageDialog(this, "Room overlaps with another room. Please adjust the position.", "Overlap Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    createRoom(newRoom);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Invalid input values",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Color getColorFromName(String colorName) {
        switch (colorName) {
            case "Red": return new Color(255, 99, 71, 200);
            case "Blue": return new Color(30, 144, 255, 200);
            case "Green": return new Color(60, 179, 113, 200);
            case "Cyan": return new Color(0, 255, 255, 200);
            case "Magenta": return new Color(255, 0, 255, 200);
            case "Yellow": return new Color(255, 255, 0, 200);
            case "Orange": return new Color(255, 165, 0, 200);
            default: return new Color(255, 255, 255, 200);
        }
    }

    private void createRoom(RoomPanel room) {
        floorPlanPanel.add(room);
        rooms.add(room);
        autoAlignToGrid(room);
        floorPlanPanel.revalidate();
        floorPlanPanel.repaint();
    }

    private void autoAlignToGrid(RoomPanel room) {
        int newX = Math.round((float) room.getX() / GRID_SPACING) * GRID_SPACING;
        int newY = Math.round((float) room.getY() / GRID_SPACING) * GRID_SPACING;
        room.setLocation(newX, newY);
    }

    private boolean checkRoomOverlap(RoomPanel newRoom) {
        for (RoomPanel room : rooms) {
            if (newRoom.getBounds().intersects(room.getBounds())) {
                return true;
            }
        }
        return false;
    }

    private void addDoorDialog() {
        if (selectedRoom == null) {
            JOptionPane.showMessageDialog(this, "Please select a room first.", "No Room Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JSpinner widthField = new JSpinner(new SpinnerNumberModel(30, 10, selectedRoom.getWidth(), 5));
        JSpinner heightField = new JSpinner(new SpinnerNumberModel(10, 10, selectedRoom.getHeight(), 5));

        String[] positionOptions = {"North", "South", "East", "West"};
        JComboBox<String> positionBox = new JComboBox<>(positionOptions);

        panel.add(new JLabel("Door Width:"));
        panel.add(widthField);
        panel.add(new JLabel("Door Height:"));
        panel.add(heightField);
        panel.add(new JLabel("Position:"));
        panel.add(positionBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Door",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            int width = ((Number) widthField.getValue()).intValue();
            int height = ((Number) heightField.getValue()).intValue();
            String position = (String) positionBox.getSelectedItem();

            if (selectedRoom.checkOverlapWithWindows(width, height, position)) {
                JOptionPane.showMessageDialog(this, "Door overlaps with a window. Please choose a different position.", "Overlap Error", JOptionPane.ERROR_MESSAGE);
            } else {
                selectedRoom.addDoor(width, height, position);
                floorPlanPanel.repaint();
            }
        }
    }

    private void addWindowDialog() {
        if (selectedRoom == null) {
            JOptionPane.showMessageDialog(this, "Please select a room first.", "No Room Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JSpinner widthField = new JSpinner(new SpinnerNumberModel(30, 10, selectedRoom.getWidth(), 5));
        JSpinner heightField = new JSpinner(new SpinnerNumberModel(10, 10, selectedRoom.getHeight(), 5));

        String[] positionOptions = {"North", "South", "East", "West"};
        JComboBox<String> positionBox = new JComboBox<>(positionOptions);

        panel.add(new JLabel("Window Width:"));
        panel.add(widthField);
        panel.add(new JLabel("Window Height:"));
        panel.add(heightField);
        panel.add(new JLabel("Position:"));
        panel.add(positionBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Window",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            int width = ((Number) widthField.getValue()).intValue();
            int height = ((Number) heightField.getValue()).intValue();
            String position = (String) positionBox.getSelectedItem();

            if (selectedRoom.checkOverlapWithDoors(width, height, position)) {
                JOptionPane.showMessageDialog(this, "Window overlaps with a door. Please choose a different position.", "Overlap Error", JOptionPane.ERROR_MESSAGE);
            } else {
                selectedRoom.addWindow(width, height, position);
                floorPlanPanel.repaint();
            }
        }
    }

    private void deleteSelectedRoom() {
        if (selectedRoom != null) {
            floorPlanPanel.remove(selectedRoom);
            rooms.remove(selectedRoom);
            selectedRoom = null;
            floorPlanPanel.revalidate();
            floorPlanPanel.repaint();
        } else {
            JOptionPane.showMessageDialog(this, "No room selected to delete.", "Delete Room", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void addFurnitureDialog() {
        if (selectedRoom == null) {
            JOptionPane.showMessageDialog(this, "Please select a room first.", "No Room Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] furnitureTypes = {"Bed", "Sink", "Dining Table"};
        JComboBox<String> furnitureBox = new JComboBox<>(furnitureTypes);

        JSpinner widthField = new JSpinner(new SpinnerNumberModel(30, 10, selectedRoom.getWidth(), 5));
        JSpinner heightField = new JSpinner(new SpinnerNumberModel(30, 10, selectedRoom.getHeight(), 5));

        JSpinner xField = new JSpinner(new SpinnerNumberModel(0, 0, selectedRoom.getWidth(), 1));
        JSpinner yField = new JSpinner(new SpinnerNumberModel(0, 0, selectedRoom.getHeight(), 1));

        panel.add(new JLabel("Furniture Type:"));
        panel.add(furnitureBox);
        panel.add(new JLabel("Width:"));
        panel.add(widthField);
        panel.add(new JLabel("Height:"));
        panel.add(heightField);
        panel.add(new JLabel("X Position:"));
        panel.add(xField);
        panel.add(new JLabel("Y Position:"));
        panel.add(yField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Furniture",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String furnitureType = (String) furnitureBox.getSelectedItem();
            int width = ((Number) widthField.getValue()).intValue();
            int height = ((Number) heightField.getValue()).intValue();
            int x = ((Number) xField.getValue()).intValue();
            int y = ((Number) yField.getValue()).intValue();

            Furniture newFurniture = new Furniture(furnitureType, x, y, width, height);
            if (selectedRoom.checkFurnitureOverlap(newFurniture)) {
                JOptionPane.showMessageDialog(this, "Furniture overlaps with another furniture. Please adjust the position.", "Overlap Error", JOptionPane.ERROR_MESSAGE);
            } else {
                selectedRoom.addFurniture(furnitureType, x, y, width, height);
                floorPlanPanel.repaint();
            }
        }
    }

    private class RoomPanel extends JPanel implements MouseListener, MouseMotionListener {
        public String roomName;
        private Point dragStart = null;
        private boolean isDragging = false;
        private final int RESIZE_BORDER = 10;
        private boolean isResizing = false;
        private int resizeEdge = 0;
        private ArrayList<Door> doors = new ArrayList<>();
        private ArrayList<Window> windows = new ArrayList<>();
        private ArrayList<Furniture> furnitures = new ArrayList<>();

        public RoomPanel(String roomName, int x, int y, int width, int height, Color color) {
            this.roomName = roomName;
            setBounds(x, y, width, height);
            setBackground(color);
            setLayout(new BorderLayout());

            JLabel roomLabel = new JLabel(roomName, JLabel.CENTER);
            roomLabel.setForeground(Color.WHITE);
            roomLabel.setFont(new Font("Arial", Font.BOLD, 14));
            add(roomLabel, BorderLayout.CENTER);

            addMouseListener(this);
            addMouseMotionListener(this);

            // Set a solid black border with a thickness of 2 pixels
            setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        }

        public void addDoor(int width, int height, String position) {
            int x = 0, y = 0;
            switch (position) {
                case "North":
                    x = (getWidth() - width) / 2;
                    y = 0;
                    break;
                case "South":
                    x = (getWidth() - width) / 2;
                    y = getHeight() - height;
                    break;
                case "East":
                    x = getWidth() - width;
                    y = (getHeight() - height) / 2;
                    break;
                case "West":
                    x = 0;
                    y = (getHeight() - height) / 2;
                    break;
            }
            doors.add(new Door(x, y, width, height));
        }

        public void addWindow(int width, int height, String position) {
            int x = 0, y = 0;
            switch (position) {
                case "North":
                    x = (getWidth() - width) / 2;
                    y = 0;
                    break;
                case "South":
                    x = (getWidth() - width) / 2;
                    y = getHeight() - height;
                    break;
                case "East":
                    x = getWidth() - width;
                    y = (getHeight() - height) / 2;
                    break;
                case "West":
                    x = 0;
                    y = (getHeight() - height) / 2;
                    break;
            }
            windows.add(new Window(x, y, width, height));
        }

        public void addFurniture(String type, int x, int y, int width, int height) {
            furnitures.add(new Furniture(type, x, y, width, height));
        }

        public boolean checkOverlapWithDoors(int width, int height, String position) {
            int x = 0, y = 0;
            switch (position) {
                case "North":
                    x = (getWidth() - width) / 2;
                    y = 0;
                    break;
                case "South":
                    x = (getWidth() - width) / 2;
                    y = getHeight() - height;
                    break;
                case "East":
                    x = getWidth() - width;
                    y = (getHeight() - height) / 2;
                    break;
                case "West":
                    x = 0;
                    y = (getHeight() - height) / 2;
                    break;
            }
            Rectangle newWindow = new Rectangle(x, y, width, height);
            for (Door door : doors) {
                if (newWindow.intersects(new Rectangle(door.x, door.y, door.width, door.height))) {
                    return true;
                }
            }
            return false;
        }

        public boolean checkOverlapWithWindows(int width, int height, String position) {
            int x = 0, y = 0;
            switch (position) {
                case "North":
                    x = (getWidth() - width) / 2;
                    y = 0;
                    break;
                case "South":
                    x = (getWidth() - width) / 2;
                    y = getHeight() - height;
                    break;
                case "East":
                    x = getWidth() - width;
                    y = (getHeight() - height) / 2;
                    break;
                case "West":
                    x = 0;
                    y = (getHeight() - height) / 2;
                    break;
            }
            Rectangle newDoor = new Rectangle(x, y, width, height);
            for (Window window : windows) {
                if (newDoor.intersects(new Rectangle(window.x, window.y, window.width, window.height))) {
                    return true;
                }
            }
            return false;
        }

        public boolean checkFurnitureOverlap(Furniture newFurniture) {
            Rectangle newFurnitureRect = new Rectangle(newFurniture.x, newFurniture.y, newFurniture.width, newFurniture.height);
            for (Furniture furniture : furnitures) {
                Rectangle existingFurnitureRect = new Rectangle(furniture.x, furniture.y, furniture.width, furniture.height);
                if (newFurnitureRect.intersects(existingFurnitureRect)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            if (this == selectedRoom) {
                g2d.setColor(Color.BLACK);
                g2d.fillRect(getWidth() - 8, getHeight() - 8, 8, 8);
            }

            g2d.setColor(Color.DARK_GRAY);
            for (Door door : doors) {
                g2d.fillRect(door.x, door.y, door.width, door.height);
            }

            g2d.setColor(Color.LIGHT_GRAY);
            for (Window window : windows) {
                g2d.fillRect(window.x, window.y, window.width, window.height);
            }

            for (Furniture furniture : furnitures) {
                switch (furniture.type) {
                    case "Bed":
                        g2d.setColor(Color.PINK);
                        g2d.fillRect(furniture.x, furniture.y, furniture.width, furniture.height);
                        break;
                    case "Sink":
                        g2d.setColor(Color.CYAN);
                        g2d.fillArc(furniture.x, furniture.y, furniture.width, furniture.height, 0, 180);
                        break;
                    case "Dining Table":
                        g2d.setColor(Color.ORANGE);
                        g2d.fillOval(furniture.x, furniture.y, furniture.width, furniture.height);
                        break;
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            selectedRoom = this;
            dragStart = e.getPoint();
            isDragging = true;

            if (e.getX() >= getWidth() - RESIZE_BORDER &&
                    e.getY() >= getHeight() - RESIZE_BORDER) {
                isResizing = true;
                resizeEdge = 3;
            } else if (e.getX() >= getWidth() - RESIZE_BORDER) {
                isResizing = true;
                resizeEdge = 1;
            } else if (e.getY() >= getHeight() - RESIZE_BORDER) {
                isResizing = true;
                resizeEdge = 2;
            }

            getParent().setComponentZOrder(this, 0);
            repaint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (!isDragging) return;

            if (isResizing) {
                int newWidth = getWidth();
                int newHeight = getHeight();

                if (resizeEdge == 1 || resizeEdge == 3) {
                    newWidth = Math.max(50, e.getX());
                }
                if (resizeEdge == 2 || resizeEdge == 3) {
                    newHeight = Math.max(50, e.getY());
                }

                newWidth = Math.round((float) newWidth / GRID_SPACING) * GRID_SPACING;
                newHeight = Math.round((float) newHeight / GRID_SPACING) * GRID_SPACING;

                setSize(newWidth, newHeight);
            } else {
                Point current = e.getLocationOnScreen();
                Point offset = getLocationOnScreen();
                int newX = getX() + (current.x - offset.x - dragStart.x);
                int newY = getY() + (current.y - offset.y - dragStart.y);

                newX = Math.max(0, Math.min(newX, getParent().getWidth() - getWidth()));
                newY = Math.max(0, Math.min(newY, getParent().getHeight() - getHeight()));

                setLocation(newX, newY);
            }

            getParent().repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (isDragging) {
                autoAlignToGrid(this);
                if (checkRoomOverlap(this)) {
                    autoAdjustPosition(this);
                }
                isDragging = false;
                isResizing = false;
                resizeEdge = 0;
                getParent().repaint();
            }
        }

        private void autoAdjustPosition(RoomPanel room) {
            for (RoomPanel otherRoom : rooms) {
                if (room != otherRoom && room.getBounds().intersects(otherRoom.getBounds())) {
                    Rectangle bounds = room.getBounds();
                    Rectangle otherBounds = otherRoom.getBounds();

                    if (bounds.intersects(otherBounds)) {
                        room.setLocation(otherBounds.x + otherBounds.width + GRID_SPACING, otherBounds.y);
                        if (!checkRoomOverlap(room)) return;

                        room.setLocation(otherBounds.x - bounds.width - GRID_SPACING, otherBounds.y);
                        if (!checkRoomOverlap(room)) return;

                        room.setLocation(otherBounds.x, otherBounds.y + otherBounds.height + GRID_SPACING);
                        if (!checkRoomOverlap(room)) return;

                        room.setLocation(otherBounds.x, otherBounds.y - bounds.height - GRID_SPACING);
                        if (!checkRoomOverlap(room)) return;
                    }
                }
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (e.getX() >= getWidth() - RESIZE_BORDER &&
                    e.getY() >= getHeight() - RESIZE_BORDER) {
                setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
            } else if (e.getX() >= getWidth() - RESIZE_BORDER) {
                setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
            } else if (e.getY() >= getHeight() - RESIZE_BORDER) {
                setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
            } else {
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }
        }

        @Override public void mouseClicked(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e) {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    private class Door {
        int x, y, width, height;

        public Door(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private class Window {
        int x, y, width, height;

        public Window(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private class Furniture {
        String type;
        int x, y, width, height;

        public Furniture(String type, int x, int y, int width, int height) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private class ColorCellRenderer extends JLabel implements ListCellRenderer<String> {
        public ColorCellRenderer() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
            setVerticalAlignment(CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list,
                                                      String value, int index, boolean isSelected, boolean cellHasFocus) {
            setText(value);
            setBackground(getColorFromName(value));
            setForeground(Color.BLACK);
            return this;
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(FloorPlanApp::new);
    }
}
