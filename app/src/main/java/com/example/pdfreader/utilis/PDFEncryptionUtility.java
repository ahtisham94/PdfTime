package com.example.pdfreader.utilis;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.example.pdfreader.R;
import com.example.pdfreader.dialogs.InputFeildDialog;
import com.example.pdfreader.interfaces.GenericCallback;

import java.io.FileOutputStream;
import java.io.IOException;


public class PDFEncryptionUtility {

    private final Activity mContext;
    private final FileUtils mFileUtils;
    private String mPassword;
    private final SharedPreferences mSharedPrefs;

    public PDFEncryptionUtility(Activity context) {
        this.mContext = context;
        this.mFileUtils = new FileUtils(context);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Opens the password mDialog to set Password for an existing PDF file.
     *
     * @param filePath Path of file to be encrypted
     */
    public void setPassword(final String filePath) {


        new InputFeildDialog(mContext, new GenericCallback() {
            @Override
            public void callback(Object o) {
                mPassword = (String) o;
                try {
                    String path = doEncryption(filePath, mPassword);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
            }
        }, "Enter Password").show();

    }

    /**
     * Uses PDF Reader to set encryption in pdf file.
     *
     * @param path     - Path of pdf file to be encrypted
     * @param password - password to be encrypted with
     * @return string - path of output file
     */
    public String doEncryption(String path, String password) throws IOException, DocumentException {

        String masterpwd = "pass@123";
        String finalOutputFile = mFileUtils.getUniqueFileName(path.replace(mContext.getString(R.string.pdf_ext),
                mContext.getString(R.string.encrypted_file)));

        PdfReader reader = new PdfReader(path);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(finalOutputFile));
        stamper.setEncryption(password.getBytes(), masterpwd.getBytes(),
                PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_COPY, PdfWriter.ENCRYPTION_AES_128);
        stamper.close();
        reader.close();
        return finalOutputFile;
    }

    /**
     * Checks if PDf is encrypted
     *
     * @param file - path of PDF file
     * @return true, if PDF is encrypted, otherwise false
     */
    private boolean isPDFEncrypted(final String file) {
        PdfReader reader;
        String ownerPass = mContext.getString(R.string.app_name);
        try {
            reader = new PdfReader(file, ownerPass.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
        //Check if PDF is encrypted or not.
        if (!reader.isEncrypted()) {
            StringUtils.getInstance().showSnackbar(mContext, R.string.not_encrypted);
            return false;
        }
        return true;
    }

    /**
     * Uses PDF Reader to decrypt the PDF.
     *
     * @param file Path of pdf file to be decrypted
     */
  /*  public void removePassword(final String file,
                               final DataSetChanged dataSetChanged) {

        if (!isPDFEncrypted(file))
            return;

        final String[] input_password = new String[1];
        mDialog.setTitle(R.string.enter_password);
        final View mPositiveAction = mDialog.getActionButton(DialogAction.POSITIVE);
        final EditText mPasswordInput = Objects.requireNonNull(mDialog.getCustomView()).findViewById(R.id.password);
        TextView text = mDialog.getCustomView().findViewById(R.id.enter_password);
        text.setText(R.string.decrypt_message);
        mPasswordInput.addTextChangedListener(
                new DefaultTextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        mPositiveAction.setEnabled(s.toString().trim().length() > 0);
                    }

                    @Override
                    public void afterTextChanged(Editable input) {
                        input_password[0] = input.toString();
                    }
                });
        mDialog.show();
        mPositiveAction.setEnabled(false);
        mPositiveAction.setOnClickListener(v -> {

            // check for password
            // our master password & their user password
            // their master password

            if (!removePasswordUsingDefMasterPassword(file, dataSetChanged, input_password)) {
                if (!removePasswordUsingInputMasterPassword(file, dataSetChanged, input_password)) {
                    StringUtils.getInstance().showSnackbar(mContext, R.string.master_password_changed);
                }
            }
            mDialog.dismiss();
        });
    }*/

    /**
     * This function removes the password for encrypted files.
     *
     * @param file          - the path of the actual encrypted file.
     * @param inputPassword - the password of the encrypted file.
     * @return - output file path
     */
 /*   public String removeDefPasswordForImages(final String file,
                                             final String[] inputPassword) {
        String finalOutputFile;
        try {
            String masterPwd = mSharedPrefs.getString(MASTER_PWD_STRING, appName);
            PdfReader reader = new PdfReader(file, masterPwd.getBytes());
            byte[] password;
            finalOutputFile = mFileUtils.getUniqueFileName
                    (file.replace(mContext.getResources().getString(R.string.pdf_ext),
                            mContext.getString(R.string.decrypted_file)));
            password = reader.computeUserPassword();
            byte[] input = inputPassword[0].getBytes();
            if (Arrays.equals(input, password)) {
                PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(finalOutputFile));
                stamper.close();
                reader.close();
                return finalOutputFile;
            }
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean removePasswordUsingDefMasterPassword(final String file,
                                                         final DataSetChanged dataSetChanged,
                                                         final String[] inputPassword) {
        String finalOutputFile;
        try {
            String masterPwd = mSharedPrefs.getString(MASTER_PWD_STRING, appName);
            PdfReader reader = new PdfReader(file, masterPwd.getBytes());
            byte[] password;
            finalOutputFile = mFileUtils.getUniqueFileName
                    (file.replace(mContext.getResources().getString(R.string.pdf_ext),
                            mContext.getString(R.string.decrypted_file)));
            password = reader.computeUserPassword();
            byte[] input = inputPassword[0].getBytes();
            if (Arrays.equals(input, password)) {
                PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(finalOutputFile));
                stamper.close();
                reader.close();
                if (dataSetChanged != null)
                    dataSetChanged.updateDataset();
                new DatabaseHelper(mContext).insertRecord(finalOutputFile, mContext.getString(R.string.decrypted));
                final String filepath = finalOutputFile;
                StringUtils.getInstance().getSnackbarwithAction(mContext, R.string.snackbar_pdfCreated)
                        .setAction(R.string.snackbar_viewAction,
                                v2 -> mFileUtils.openFile(filepath, FileUtils.FileType.e_PDF)).show();
                return true;
            }
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }

        return false;
    }


    private boolean removePasswordUsingInputMasterPassword(final String file,
                                                           final DataSetChanged dataSetChanged,
                                                           final String[] inputPassword) {
        String finalOutputFile;
        try {
            PdfReader reader = new PdfReader(file, inputPassword[0].getBytes());
            finalOutputFile = mFileUtils.getUniqueFileName(
                    file.replace(mContext.getResources().getString(R.string.pdf_ext),
                            mContext.getString(R.string.decrypted_file)));
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(finalOutputFile));
            stamper.close();
            reader.close();
            if (dataSetChanged != null)
                dataSetChanged.updateDataset();
            new DatabaseHelper(mContext).insertRecord(finalOutputFile, mContext.getString(R.string.decrypted));
            final String filepath = finalOutputFile;
            StringUtils.getInstance().getSnackbarwithAction(mContext, R.string.snackbar_pdfCreated)
                    .setAction(R.string.snackbar_viewAction, v2 ->
                            mFileUtils.openFile(filepath, FileUtils.FileType.e_PDF)).show();
            return true;

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }*/
}
