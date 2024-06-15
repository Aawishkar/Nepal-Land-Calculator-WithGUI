import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

class LandUnitConverter {
    private static final Map<String, Double> UNITS;
    private static final Map<String, String> UNIT_DESCRIPTIONS;

    static {
        UNITS = new HashMap<>();
        UNITS.put("ropani", 508.72);
        UNITS.put("aana", 31.80);
        UNITS.put("paisa", 7.95);
        UNITS.put("daam", 1.99);
        UNITS.put("bigha", 6772.63);
        UNITS.put("katha", 338.63);
        UNITS.put("dhur", 16.93);

        UNIT_DESCRIPTIONS = new HashMap<>();
        UNIT_DESCRIPTIONS.put("ropani", "Ropani: Traditional unit of area in Nepal.");
        UNIT_DESCRIPTIONS.put("aana", "Aana: 1/16 of a ropani.");
        UNIT_DESCRIPTIONS.put("paisa", "Paisa: 1/4 of an aana.");
        UNIT_DESCRIPTIONS.put("daam", "Daam: 1/4 of a paisa.");
        UNIT_DESCRIPTIONS.put("bigha", "Bigha: Traditional unit of area in Terai region.");
        UNIT_DESCRIPTIONS.put("katha", "Katha: 1/20 of a bigha.");
        UNIT_DESCRIPTIONS.put("dhur", "Dhur: 1/20 of a katha.");
    }

    public double toSquareMeters(double value, String unit) {
        return value * UNITS.get(unit);
    }

    public double fromSquareMeters(double value, String unit) {
        return value / UNITS.get(unit);
    }

    public double convert(double value, String fromUnit, String toUnit) {
        if (fromUnit.equals(toUnit)) {
            return value;
        }
        if (UNITS.containsKey(fromUnit) && UNITS.containsKey(toUnit)) {
            double valueInSqM = toSquareMeters(value, fromUnit);
            return fromSquareMeters(valueInSqM, toUnit);
        }
        throw new IllegalArgumentException("Unsupported units for conversion");
    }

    public Map<String, Double> convertToAll(double value, String fromUnit) {
        Map<String, Double> conversions = new HashMap<>();
        if (UNITS.containsKey(fromUnit)) {
            double valueInSqM = toSquareMeters(value, fromUnit);
            for (String unit : UNITS.keySet()) {
                conversions.put(unit, fromSquareMeters(valueInSqM, unit));
            }
        } else {
            throw new IllegalArgumentException("Unsupported unit for conversion");
        }
        return conversions;
    }

    public static String[] getUnits() {
        return UNITS.keySet().toArray(new String[0]);
    }

    public static String getUnitDescription(String unit) {
        return UNIT_DESCRIPTIONS.get(unit);
    }
}

public class LandUnitConverterApp extends JFrame {
    private LandUnitConverter converter;
    private JTextField valueField;
    private JComboBox<String> fromUnitCombo;
    private JComboBox<String> toUnitCombo;
    private JTextArea resultArea;
    private JTextArea historyArea;

    public LandUnitConverterApp() {
        converter = new LandUnitConverter();

        setTitle("Land Unit Converter for Nepal");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel inputPanel = createInputPanel();
        add(inputPanel, BorderLayout.NORTH);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("SansSerif", Font.PLAIN, 16));
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font("SansSerif", Font.ITALIC, 12));
        add(new JScrollPane(historyArea), BorderLayout.SOUTH);

        getContentPane().setBackground(new Color(230, 230, 250));
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        inputPanel.setBackground(new Color(245, 245, 245));

        JLabel valueLabel = new JLabel("Value:");
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        inputPanel.add(valueLabel);
        valueField = new JTextField();
        valueField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                performConversion();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                performConversion();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                performConversion();
            }
        });
        inputPanel.add(valueField);

        JLabel fromUnitLabel = new JLabel("From Unit:");
        fromUnitLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        inputPanel.add(fromUnitLabel);
        fromUnitCombo = new JComboBox<>(LandUnitConverter.getUnits());
        fromUnitCombo.addActionListener(e -> updateUnitDescription());
        inputPanel.add(fromUnitCombo);

        JLabel toUnitLabel = new JLabel("To Unit (for single conversion):");
        toUnitLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        inputPanel.add(toUnitLabel);
        toUnitCombo = new JComboBox<>(LandUnitConverter.getUnits());
        toUnitCombo.addActionListener(e -> performConversion());
        inputPanel.add(toUnitCombo);

        JButton convertButton = new JButton("Convert", new ImageIcon("convert-icon.png"));
        convertButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        convertButton.addActionListener(new ConvertActionListener());
        inputPanel.add(convertButton);

        JButton convertToAllButton = new JButton("Convert to All", new ImageIcon("convert-all-icon.png"));
        convertToAllButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        convertToAllButton.addActionListener(new ConvertToAllActionListener());
        inputPanel.add(convertToAllButton);

        JButton clearButton = new JButton("Clear", new ImageIcon("clear-icon.png"));
        clearButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        clearButton.addActionListener(new ClearActionListener());
        inputPanel.add(clearButton);

        return inputPanel;
    }

    private void displayErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void updateUnitDescription() {
        String fromUnit = fromUnitCombo.getSelectedItem().toString();
        String description = LandUnitConverter.getUnitDescription(fromUnit);
        valueField.setToolTipText(description);
    }

    private void performConversion() {
        try {
            double value = Double.parseDouble(valueField.getText());
            String fromUnit = fromUnitCombo.getSelectedItem().toString().toLowerCase();
            String toUnit = toUnitCombo.getSelectedItem().toString().toLowerCase();
            double result = converter.convert(value, fromUnit, toUnit);
            resultArea.setText(String.format("%f %s is equal to %f %s", value, fromUnit, result, toUnit));
        } catch (NumberFormatException ex) {
            resultArea.setText("Invalid input value. Please enter a numeric value.");
        } catch (IllegalArgumentException ex) {
            resultArea.setText(ex.getMessage());
        }
    }

    private void addToHistory(String conversion) {
        historyArea.append(conversion + "\n");
    }

    private class ConvertActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                double value = Double.parseDouble(valueField.getText());
                String fromUnit = fromUnitCombo.getSelectedItem().toString().toLowerCase();
                String toUnit = toUnitCombo.getSelectedItem().toString().toLowerCase();
                double result = converter.convert(value, fromUnit, toUnit);
                String conversion = String.format("%f %s is equal to %f %s", value, fromUnit, result, toUnit);
                resultArea.setText(conversion);
                addToHistory(conversion);
            } catch (NumberFormatException ex) {
                displayErrorMessage("Invalid input value. Please enter a numeric value.");
            } catch (IllegalArgumentException ex) {
                displayErrorMessage(ex.getMessage());
            }
        }
    }

    private class ConvertToAllActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                double value = Double.parseDouble(valueField.getText());
                String fromUnit = fromUnitCombo.getSelectedItem().toString().toLowerCase();
                Map<String, Double> conversions = converter.convertToAll(value, fromUnit);
                StringBuilder results = new StringBuilder();
                results.append(String.format("Conversions for %f %s:\n", value, fromUnit));
                for (Map.Entry<String, Double> entry : conversions.entrySet()) {
                    results.append(String.format("%f %s\n", entry.getValue(), entry.getKey()));
                }
                resultArea.setText(results.toString());
                addToHistory(results.toString());
            } catch (NumberFormatException ex) {
                displayErrorMessage("Invalid input value. Please enter a numeric value.");
            } catch (IllegalArgumentException ex) {
                displayErrorMessage(ex.getMessage());
            }
        }
    }

    private class ClearActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            valueField.setText("");
            fromUnitCombo.setSelectedIndex(0);
            toUnitCombo.setSelectedIndex(0);
            resultArea.setText("");
            historyArea.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LandUnitConverterApp app = new LandUnitConverterApp();
            app.setVisible(true);
        });
    }
}
