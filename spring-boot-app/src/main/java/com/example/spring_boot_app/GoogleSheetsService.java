package com.example.spring_boot_app;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleSheetsService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleSheetsService.class);

    private Sheets sheetsService;

    @Value("${google.credentials.path}")
    private Resource credentialsPath;

    @Value("${google.spreadsheet.id}")
    private String spreadsheetId;

    @PostConstruct
    private void init() throws IOException, GeneralSecurityException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsPath.getInputStream())
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
        sheetsService = new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("Spring Boot Google Sheets Integration")
                .build();
    }

    /**
     * Reads data from the specified range in the Google Sheets spreadsheet.
     *
     * @param range The range to read from (e.g., "Users!A1:D10").
     * @return A list of lists containing the cell values.
     */
    public List<List<Object>> readSheetData(String range) {
        try {
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            return response.getValues();
        } catch (IOException e) {
            logger.error("Failed to read data from Google Sheets: {}", e.getMessage());
            throw new RuntimeException("Failed to read data from Google Sheets", e);
        }
    }

    /**
     * Writes data to the specified range in the Google Sheets spreadsheet.
     *
     * @param range  The range to write to (e.g., "Users!A1").
     * @param values A list of lists containing the values to write.
     */
    public void writeSheetData(String range, List<List<Object>> values) {
        try {
            ValueRange body = new ValueRange().setValues(values);
            sheetsService.spreadsheets().values()
                    .update(spreadsheetId, range, body)
                    .setValueInputOption("RAW")
                    .execute();
            logger.info("Data written successfully to range: {}", range);
        } catch (IOException e) {
            logger.error("Failed to write data to Google Sheets: {}", e.getMessage());
            throw new RuntimeException("Failed to write data to Google Sheets", e);
        }
    }

    /**
     * Appends a row to the Google Sheets spreadsheet.
     *
     * @param range   The sheet name or range to append data (e.g., "Users").
     * @param rowData A list of objects representing a single row to append.
     */
    public void appendRow(String range, List<Object> rowData) {
        try {
            ValueRange appendBody = new ValueRange().setValues(Collections.singletonList(rowData));
            sheetsService.spreadsheets().values()
                    .append(spreadsheetId, range, appendBody)
                    .setValueInputOption("RAW") // or "USER_ENTERED" for automatic formatting
                    .setInsertDataOption("INSERT_ROWS") // Ensures rows are added rather than replacing data
                    .execute();
            logger.info("Row appended successfully to range: {}", range);
        } catch (IOException e) {
            logger.error("Failed to append row to Google Sheets: {}", e.getMessage());
            throw new RuntimeException("Failed to append row to Google Sheets", e);
        }
    }
}
