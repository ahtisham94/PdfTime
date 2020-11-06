package com.techlogix.pdftime.utilis;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.techlogix.pdftime.R;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.techlogix.pdftime.utilis.Constants.STORAGE_LOCATION;
import static com.techlogix.pdftime.utilis.Constants.docExtension;
import static com.techlogix.pdftime.utilis.Constants.docxExtension;
import static com.techlogix.pdftime.utilis.Constants.excelExtension;
import static com.techlogix.pdftime.utilis.Constants.excelWorkbookExtension;
import static com.techlogix.pdftime.utilis.Constants.folderDirectory;
import static com.techlogix.pdftime.utilis.Constants.pdfDirectory;
import static com.techlogix.pdftime.utilis.Constants.pdfExtension;
import static com.techlogix.pdftime.utilis.Constants.textExtension;

public class DirectoryUtils {

    private final Context mContext;
    private final SharedPreferences mSharedPreferences;
    private ArrayList<String> mFilePaths;
    private ArrayList<File> fileArrayList = new ArrayList<>();
    private ArrayList<File> selectedFiles = new ArrayList<>();

    public DirectoryUtils(Context context) {
        mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Used to search for PDF matching the search query
     *
     * @param query - Query from search bar
     * @return ArrayList containing all the pdf files matching the search query
     */
    ArrayList<File> searchPDF(String query) {
        ArrayList<File> searchResult = new ArrayList<>();
        final File[] files = getOrCreatePdfDirectory().listFiles();
        ArrayList<File> pdfs = searchPdfsFromPdfFolder(files);
        for (File pdf : pdfs) {
            String path = pdf.getPath();
            String[] fileName = path.split("/");
            String pdfName = fileName[fileName.length - 1].replace("pdf", "");
            if (checkChar(query, pdfName) == 1) {
                searchResult.add(pdf);
            }
        }
        return searchResult;
    }

    /**
     * Used in searchPDF to give the closest result to search query
     *
     * @param query    - Query from search bar
     * @param fileName - name of PDF file
     * @return 1 if the search query and filename has same characters , otherwise 0
     */
    private int checkChar(String query, String fileName) {
        query = query.toLowerCase();
        fileName = fileName.toLowerCase();
        Set<Character> q = new HashSet<>();
        Set<Character> f = new HashSet<>();
        for (char c : query.toCharArray()) {
            q.add(c);
        }
        for (char c : fileName.toCharArray()) {
            f.add(c);
        }

        if (q.containsAll(f) || f.containsAll(q))
            return 1;

        return 0;
    }

    // RETURNING LIST OF FILES OR DIRECTORIES

    /**
     * Returns pdf files from folder
     *
     * @param files list of files (folder)
     */
    ArrayList<File> getPdfsFromPdfFolder(File[] files) {
        ArrayList<File> pdfFiles = new ArrayList<>();
        if (files == null)
            return pdfFiles;
        for (File file : files) {
            if (isPDFAndNotDirectory(file))
                pdfFiles.add(file);
        }
        return pdfFiles;
    }

    private ArrayList<File> searchPdfsFromPdfFolder(File[] files) {
        ArrayList<File> pdfFiles = getPdfsFromPdfFolder(files);
        if (files == null)
            return pdfFiles;
        for (File file : files) {
            if (file.isDirectory()) {
                for (File dirFiles : file.listFiles()) {
                    if (isPDFAndNotDirectory(dirFiles))
                        pdfFiles.add(dirFiles);
                }
            }
        }
        return pdfFiles;
    }

    /**
     * Checks if a given file is PDF
     *
     * @param file - input file
     * @return tru - if condition satisfies, else false
     */
    private boolean isPDFAndNotDirectory(File file) {
        return !file.isDirectory() &&
                file.getName().endsWith(mContext.getString(R.string.pdf_ext));
    }

    /**
     * create PDF directory if directory does not exists
     */
    public File getOrCreatePdfDirectory() {
        File folder = new File(mSharedPreferences.getString(STORAGE_LOCATION,
                StringUtils.getInstance().getDefaultStorageLocation()));
        if (!folder.exists())
            folder.mkdir();
        return folder;
    }

    /**
     * get the PDF files stored in directories other than home directory
     *
     * @return ArrayList of PDF files
     */
    public ArrayList<File> getPdfFromOtherDirectories() {
        mFilePaths = new ArrayList<>();
        walkDir(getOrCreatePdfDirectory());
        ArrayList<File> files = new ArrayList<>();
        for (String path : mFilePaths)
            files.add(new File(path));
        return files;
    }


    /**
     * gets a list of all the pdf files on the user device
     *
     * @return - list of file absolute paths
     */
    ArrayList<String> getAllPDFsOnDevice() {
        mFilePaths = new ArrayList<>();
        walkDir(Environment.getExternalStorageDirectory());
        return mFilePaths;
    }

    /**
     * Walks through given dir & sub directory, and append file path to mFilePaths
     *
     * @param dir - root directory
     */
    private void walkDir(File dir) {
        walkDir(dir, Collections.singletonList(pdfExtension));
    }

    /**
     * Walks through given dir & sub direc, and append file path to mFilePaths
     *
     * @param dir        - root directory
     * @param extensions - a list of file extensions to search for
     */
    private void walkDir(File dir, List<String> extensions) {
        File[] listFile = dir.listFiles();
        if (listFile != null) {
            for (File aListFile : listFile) {

                if (aListFile.isDirectory()) {
                    walkDir(aListFile, extensions);
                } else {
                    for (String extension : extensions) {
                        if (aListFile.getName().endsWith(extension)) {
                            //Do what ever u want
                            mFilePaths.add(aListFile.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

    /**
     * gets a list of all the excel files on the user device
     *
     * @return - list of file absolute paths
     */
    ArrayList<String> getAllExcelDocumentsOnDevice() {
        mFilePaths = new ArrayList<>();
        walkDir(Environment.getExternalStorageDirectory(), Arrays.asList(excelExtension, excelWorkbookExtension));
        return mFilePaths;
    }

    /**
     * creates new folder for temp files
     */
    public static void makeAndClearTemp() {
        String dest = Environment.getExternalStorageDirectory().toString() +
                pdfDirectory + Constants.tempDirectory;
        File folder = new File(dest);
        boolean result = folder.mkdir();

        // clear all the files in it, if any
        if (result && folder.isDirectory()) {
            String[] children = folder.list();
            for (String child : children) {
                new File(folder, child).delete();
            }
        }
    }

    public ArrayList<File> searchDir(File dir) {
        File FileList[] = dir.listFiles();

        if (FileList != null) {
            for (int i = 0; i < FileList.length; i++) {

                if (FileList[i].isDirectory()) {
                    searchDir(FileList[i]);
                } else {
                    if (FileList[i].getName().endsWith(pdfExtension) ||
                            FileList[i].getName().endsWith(textExtension) ||
                            FileList[i].getName().endsWith(excelExtension) ||
                            FileList[i].getName().endsWith(excelWorkbookExtension) ||
                            FileList[i].getName().endsWith(docExtension) ||
                            FileList[i].getName().endsWith(docxExtension)
                    ) {
                        //here you have that file.
                        fileArrayList.add(FileList[i]);

                    }
                }
            }
            return fileArrayList;
        }
        return null;
    }

    public ArrayList<File> getSelectedFiles(File file, String type) {
        File FileList[] = file.listFiles();
        String[] types = type.split(",");
        if (FileList != null) {
            for (int i = 0; i < FileList.length; i++) {

                if (FileList[i].isDirectory()) {
                    getSelectedFiles(FileList[i], type);
                } else {
                    if (FileList[i].getName().endsWith(types[0]) || FileList[i].getName().endsWith(types[1])) {
                        //here you have that file.
                        selectedFiles.add(FileList[i]);

                    }
                }
            }
            return selectedFiles;
        }
        return null;
    }

    public void clearSelectedArray() {
        selectedFiles = new ArrayList<>();
    }


    public boolean renameFile(File file, String rename) {
        File from = new File(file.getAbsolutePath());
        File to = new File(file.getParent(), rename + "." + file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".")).replace(".", ""));
        return from.renameTo(to);
    }

    public File createFolder(String folderName) {
        //create folder
        File file = new File(Environment.getExternalStorageDirectory().toString(), folderDirectory + folderName);
        if (!file.exists()) {
            file.mkdirs();
        }
        String filePath = file.getAbsolutePath() + File.separator + folderName;
        return file;
    }

    public ArrayList<File> getAllFolders() {
        ArrayList<File> allFolders = new ArrayList<>();
        File f = new File(Environment.getExternalStorageDirectory() + folderDirectory);
        File[] files = f.listFiles();
        if (files != null) {
            for (File inFile : files) {
                if (inFile.isDirectory()) {
                    allFolders.add(inFile);
                }
            }
        }

        return allFolders;
    }

    public boolean deleteFile(File file) {
        File file1 = new File(file.getAbsolutePath());
        if (file1.exists())
            return file.delete();
        return false;
    }

    public boolean moveFile(String inputPath, String inputFile, String outputPath) {

        InputStream in = null;
        OutputStream out = null;
        try {

            //create output directory if it doesn't exist
            File dir = new File(outputPath + inputFile);
            if (!dir.exists()) {
                dir.createNewFile();
            }


            in = new FileInputStream(inputPath);
            out = new FileOutputStream(dir);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            // delete the original file
//            new File(inputPath).delete();
            deleteFile(new File(inputPath));
            return true;

        } catch (Exception fnfe1) {
            Log.e("tag", fnfe1.getMessage() + "");
            return false;
        }


    }


    public File createExcelToPdf(File file) {
        try {
            File pdfFile = new File(file.getParent(), "test.pdf");
            if (!pdfFile.exists()) {
                pdfFile.createNewFile();
            }
            FileInputStream stream = new FileInputStream(file);
            POIFSFileSystem myFileSystem = new POIFSFileSystem(stream);
            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);
            HSSFSheet mySheet = myWorkBook.getSheetAt(0);
            Iterator<Row> rowIter = mySheet.rowIterator();
            OutputStream outputStream = new FileOutputStream(pdfFile);
            Document document = null;
            Font font = null;
            PdfPTable table = new PdfPTable(mySheet.getRow(0).getLastCellNum());
            if (mySheet.getRow(0).getLastCellNum() > 10) {
                document = new Document(PageSize.A4_LANDSCAPE);
                font = FontFactory.getFont(FontFactory.HELVETICA, 8);
                table.setWidthPercentage(100f);
            } else {

                document = new Document(PageSize.A4);
                font = FontFactory.getFont(FontFactory.HELVETICA, 8);
                table.setWidthPercentage(150f);
            }
            PdfWriter.getInstance(document, outputStream);
            document.open();

            for (int i = 0; i <= mySheet.getLastRowNum(); i++) {
                HSSFRow row = mySheet.getRow(i);
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    HSSFCell cell = row.getCell(j);
                    PdfPCell pdfPCell = new PdfPCell(new Phrase(cell.toString(), font));

                    table.addCell(pdfPCell);
                }

            }

            document.add(table);
            document.close();
            return pdfFile;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
