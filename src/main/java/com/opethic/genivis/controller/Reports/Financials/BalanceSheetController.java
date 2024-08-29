package com.opethic.genivis.controller.Reports.Financials;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opethic.genivis.network.APIClient;
import com.opethic.genivis.network.EndPoints;
import com.opethic.genivis.network.RequestType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.JsonParser;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.input.MouseEvent;

public class BalanceSheetController implements Initializable {

    @FXML
    private TableView<Liability> table1;

//    private TableView<Liability> table1;
    private ObservableList<Liability> liabilitiesData;
//    ObservableList<Liability> liabilitiesData


    @FXML
    private TableView<Assets> table2;

    @FXML
    private TableColumn<Liability, String> liabilitiesCol;

    @FXML
    private TableColumn<Liability, String> libAmtCol;

    @FXML
    private TableColumn<Assets, String> assetsCol;

    @FXML
    private TableColumn<Assets, String> assetsAmtCol;

    @FXML
    private BorderPane bpRootPane;


    @FXML
    private Rectangle rectangle;
    private boolean isRectangleVisible = false;
    private static final Logger logger = LogManager.getLogger(BalanceSheetController.class);
    private JsonObject jsonObject = null;

    public static class Liability {
        private final String liability;
        private final String amount;
        private final String id;

        public Liability(String liability, String amount,String id) {
            this.liability = liability;
            this.amount = amount;
            this.id = id;
        }

        public String getLiability() {
            return liability;
        }

        public String getAmount() {
            return amount;
        }

        public String getId() {
            return id;
        }
    }

    public static class Assets {
        private final String assets;
        private final String assets_amount;

        public Assets(String assets, String assets_amount) {
            this.assets = assets;
            this.assets_amount = assets_amount;
        }

        public String getAssets() {
            return assets;
        }

        public String getAssetsAmount() {
            return assets_amount;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set up the columns in table1
        liabilitiesCol.setCellValueFactory(new PropertyValueFactory<>("liability"));
        libAmtCol.setCellValueFactory(new PropertyValueFactory<>("amount"));



        // Set up the columns in table2
        assetsCol.setCellValueFactory(new PropertyValueFactory<>("assets"));
        assetsAmtCol.setCellValueFactory(new PropertyValueFactory<>("assetsAmount"));

        // Add static data to table2
        ObservableList<Assets> assetsData = FXCollections.observableArrayList(
                new Assets("Fixed Assets", "0.00"),
                new Assets("Investments", "0.00"),
                new Assets("Current Assets", "80.00")
        );
        table2.setItems(assetsData);



        getBalanceSheetData();

        // Handle row click
        table1.setRowFactory(tv -> {
            TableRow<Liability> row = new TableRow<>();
            row.setOnMouseClicked(event -> handleRowClick(event, row));
            return row;
        });

    }


    public void getBalanceSheetData() {
        APIClient apiClient = null;
        try {
            apiClient = new APIClient(EndPoints.BALANCE_SHEET_LIST, "", RequestType.FORM_DATA);

            apiClient.setOnSucceeded(event -> {
                JsonObject jsonObject = new Gson().fromJson(event.getSource().getValue().toString(), JsonObject.class);
                if (jsonObject.get("responseStatus").getAsInt() == 200) {
//                    String capital_account = jsonObject.get("capital_account").toString();
//                    String loans = jsonObject.get("loans").toString();
//                    String current_liabilities = jsonObject.get("current_liabilities").toString();

//                    System.out.println(jsonObject.get("capital_account"));
                    // Add static data to table1
                   liabilitiesData = FXCollections.observableArrayList(
                            new Liability("Capital Account", jsonObject.get("capital_account").toString(),jsonObject.get("capital_account_id").toString()),
                            new Liability("Loans(Liabilities)", jsonObject.get("loans").toString(),jsonObject.get("loans_id").toString()),
                            new Liability("Current Liabilities", jsonObject.get("current_liabilities").toString(),jsonObject.get("current_liabilities_id").toString()),
                            new Liability("Profit & Loss A/c", jsonObject.get("salesAC").toString(),""),
                            new Liability("Opening Balance", "",""),
                            new Liability("Current Period", "","")
                    );

                    table1.setItems(liabilitiesData);
                } else {
                    // Handle API failure
                    System.err.println("Failed to connect to server!");
                }
            });

            apiClient.setOnCancelled(event -> System.err.println("Network API cancelled in getBalanceSheetData: " + event.getSource().getValue()));

            apiClient.setOnFailed(event -> System.err.println("Network API failed in getBalanceSheetData: " + event.getSource().getValue()));

            apiClient.start();
        } catch (Exception e) {
            System.err.println("Failed to connect to server!");
            e.printStackTrace(); // Optionally, use logging here instead of printing stack traces
        } finally {
            apiClient = null; // Reset the API client to null to avoid memory leaks
        }
    }

    // Example method to retrieve liabilitiesData


    // Example method to add an item to liabilitiesData
    public void addItemToLiabilitiesData(Liability item) {
        liabilitiesData.add(item);
    }
    private void handleRowClick(MouseEvent event, TableRow<Liability> row) {
        Liability row1 = table1.getSelectionModel().getSelectedItem();
        if (row1 != null) {
//            int row1Index = row1.getIndex();
            int selectedIndex = table1.getSelectionModel().getSelectedIndex();


            System.out.println(liabilitiesData);

            // Add an empty row below the clicked row
            liabilitiesData.add(selectedIndex + 1, new Liability("", "",""));
        }
    }



//    private void showAlert(String message, AlertUtility.AlertType alertType) {
//        Stage stage = (Stage) bpRootPane.getScene().getWindow();
//        AlertUtility.AlertError(stage, alertType, message, number -> {
//            if (number == 0) {
//                // Handle the callback if needed
//            }
//        });
//    }


    // TODO: Function to load the all company list

}
