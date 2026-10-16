package wns.automation.dataprovider;

import org.apache.commons.beanutils.Converter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.DataProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

public class ExcelDataProviderCustom implements Iterator {

    private Sheet sheet;
    private Iterator<Row> rowIterator;
    private Class[] parameterTypes;
    private Converter[] parameterConverters;

    @DataProvider(name = "ExcelDataUsingMaps")
    public static Iterator<Object[]> getExcelData(Method m) {

        Map<Integer, Map<String, String>> sheetData = null;
        System.out.println(m.getDeclaringClass().toString());
        if (m.getDeclaringClass().toString().contains("testLoginWithExcelData")) {
            sheetData = readExcelSheet("src/test/java/testData/skillMatrixLogin.xls", "Sheet1");
        }
        else if (m.getDeclaringClass().toString().contains("testMainCategory")) {
            sheetData = readExcelSheet("src/test/java/testData/testSkillMatrix.xls", "Sheet1");
        }
        else if (m.getDeclaringClass().toString().contains("testSubCategory")) {
            sheetData = readExcelSheet("src/test/java/testData/testSkillMatrix.xls", "Sheet1");
        }
        else if (m.getDeclaringClass().toString().contains("testSkills")) {
            sheetData = readExcelSheet("src/test/java/testData/testSkillMatrix.xls", "Sheet1");
        }
        else if (m.getDeclaringClass().toString().contains("testLoginPlaywright")) {
            sheetData = readExcelSheet("src/test/java/testData/testSkillMatrix.xls", "Sheet1");
        }
        else if (m.getDeclaringClass().toString().contains("OHRMLoginTestPW")) {
            sheetData = readExcelSheet("src/test/java/testData/testOHRM.xls", "Sheet1");
        }

        else if (m.getDeclaringClass().toString().contains("SMLoginTestPW")) {sheetData = readExcelSheet(
                    "src/test/java/testData/testSkillMatrix.xls",
                    "Sheet1");
        }

        List<Object[]> excelData = new ArrayList<Object[]>();
        assert sheetData != null;
        for (Map<String, String> rowData : sheetData.values()) {
            String executionStatus = rowData.get("ExecutionStatus");
            if (null != executionStatus && "y".equalsIgnoreCase(executionStatus.trim())) {
                excelData.add(new Object[] { rowData });
            }
        }
        return excelData.iterator();
    }

    @Override
    public boolean hasNext() {
        return rowIterator.hasNext();
    }

    @Override
    public Object next() {
        String[] data;
        if (rowIterator != null && rowIterator.hasNext()) {
            Object[] args = parseLine(rowIterator.next());
            System.out.println(Arrays.toString(args));
            return args;
        }
        return null;
    }

    private Object[] parseLine(Row row) {
        int len = row.getPhysicalNumberOfCells();
        Object[] ovals = new Object[len];
        for (int i = 0; i < len; i++) {
            ovals[i] = parameterConverters[i].convert(parameterTypes[i], row.getCell(i));
        }
        return ovals;
    }

    public static Map<Integer, Map<String, String>> readExcelSheet(String path, String sheetName){
        Map<Integer, Map<String, String>> sheetData = new LinkedHashMap<Integer, Map<String, String>>();
        try (FileInputStream inputStream = new FileInputStream(new File(path));
             Workbook book = getWorkbook(inputStream, path)) {
            Sheet sheet = book.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException(
                        "Worksheet '" + sheetName + "' was not found in " + path);
            }
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("Worksheet '" + sheetName + "' has no header row: " + path);
            }
            int columnCount = headerRow.getLastCellNum();
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                Row currentRow = sheet.getRow(i);
                if (currentRow == null) {
                    continue;
                }
                Map<String, String> rowData = new LinkedHashMap<>();
                for (int j = 0; j < columnCount; j++) {
                    Cell headerCell = headerRow.getCell(j);
                    if (headerCell == null) {
                        continue;
                    }
                    String header = headerCell.getStringCellValue();
                    Cell currentCell = currentRow.getCell(j);
                    String value = "";
                    if (currentCell != null) {
                        currentCell.setCellType(CellType.STRING);
                        value = currentCell.getStringCellValue();
                    }
                    rowData.put(header, value);
                }
                sheetData.put(i, rowData);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read Excel test data from " + path, ex);
        }
        return sheetData;
    }
    private static Workbook getWorkbook(FileInputStream inputStream, String excelFilePath)
            throws IOException {
        Workbook workbook = null;

        if (excelFilePath.endsWith("xlsx")) {
            workbook = new XSSFWorkbook(inputStream);
        }

        else if (excelFilePath.endsWith("xls")) {
            workbook = new HSSFWorkbook(inputStream);
        } else {
            throw new IllegalArgumentException("The specified file is not Excel file");
        }
        return workbook;
    }
}