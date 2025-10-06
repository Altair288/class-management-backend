package com.altair288.class_management.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportStudentsResult {
    private int totalRows;          // 含表头的总行数(或数据行数依据语义)
    private int processed;          // 参与处理的有效数据行（非纯空）
    private int success;            // 成功插入数量
    private List<String> duplicateStudentNos = new ArrayList<>();
    private List<RowError> rowErrors = new ArrayList<>();

    public void incSuccess(){ success++; }
    public void incProcessed(){ processed++; }
    public void setTotalRows(int totalRows){ this.totalRows = totalRows; }
    public void addDuplicate(String no){ if(!duplicateStudentNos.contains(no)) duplicateStudentNos.add(no); }
    public void addRowError(int rowIndex, String error){ rowErrors.add(new RowError(rowIndex, error)); }

    public int getTotalRows() { return totalRows; }
    public int getProcessed() { return processed; }
    public int getSuccess() { return success; }
    public List<String> getDuplicateStudentNos() { return duplicateStudentNos; }
    public List<RowError> getRowErrors() { return rowErrors; }

    public static class RowError {
        private int rowIndex; // Excel 中的行号（从1开始还是原始，这里使用实际行号(首行为1)）
        private String error;
        public RowError(int rowIndex, String error){ this.rowIndex = rowIndex; this.error = error; }
        public int getRowIndex(){ return rowIndex; }
        public String getError(){ return error; }
    }
}
