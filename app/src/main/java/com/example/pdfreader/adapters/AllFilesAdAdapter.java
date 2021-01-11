package com.example.pdfreader.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pdfreader.MainActivity;
import com.example.pdfreader.PDFViewerAcitivity;
import com.example.pdfreader.R;
import com.example.pdfreader.SearchPdfFileActivity;
import com.example.pdfreader.SharePrefData;
import com.example.pdfreader.dialogs.AlertDialogHelper;
import com.example.pdfreader.dialogs.CreateFolderDialog;
import com.example.pdfreader.dialogs.MoveFileDialog;
import com.example.pdfreader.fragments.dashboardFragments.FileFragment;
import com.example.pdfreader.interfaces.GenericCallback;
import com.example.pdfreader.models.FileInfoModel;
import com.example.pdfreader.utilis.Constants;
import com.example.pdfreader.utilis.DirectoryUtils;
import com.example.pdfreader.utilis.FileInfoUtils;
import com.example.pdfreader.utilis.NormalUtils;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AllFilesAdAdapter extends RecyclerView.Adapter<AllFilesAdAdapter.MyFilesHolder> {

    Context context;
    ArrayList<FileInfoModel> filesArrayList, checkBoxArray;
    DirectoryUtils mDirectory;
    RecyclerView recyclerView;
    GenericCallback callback;
    boolean showCheckbox = false;

    MyFilesHolder myFilesHolder;
    UnifiedNativeAd googlead;
    NativeAd fbad;

    public void setCallback(GenericCallback callback) {
        this.callback = callback;
    }

    public AllFilesAdAdapter(Context context, ArrayList<FileInfoModel> filesArrayList) {
        this.context = context;
        this.filesArrayList = filesArrayList;
        mDirectory = new DirectoryUtils(context);
        checkBoxArray = new ArrayList<>();
    }




    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public MyFilesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.files_item_layout_ad, parent, false);
        myFilesHolder=new MyFilesHolder(view);
        return myFilesHolder;
    }

    public void setData(ArrayList<FileInfoModel> filesArrayList) {
        if(filesArrayList!=null) {
            this.filesArrayList = new ArrayList<>();
            this.filesArrayList = filesArrayList;
            notifyDataSetChanged();
        }
    }

    public void setShowCheckbox(boolean showMoreBtn) {
        this.showCheckbox = showMoreBtn;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyFilesHolder holder, final int position) {
        if (showCheckbox)
            holder.setViewsAllignment();
        if (filesArrayList.get(holder.getAdapterPosition()).getSelect()) {
            holder.rootLayout.setBackgroundColor(context.getResources().getColor(R.color.colorGrayHightted));
        } else
            holder.rootLayout.setBackgroundColor(context.getResources().getColor(R.color.colorWhite));

        holder.fileNameTv.setText(filesArrayList.get(holder.getAdapterPosition()).getFileName());
        holder.sizeTv.setText(FileInfoUtils.getFormattedSize(filesArrayList.get(holder.getAdapterPosition()).getFile()));
        holder.dateTv.setText(FileInfoUtils.getFormattedDate(filesArrayList.get(holder.getAdapterPosition()).getFile()));
        if (filesArrayList.get(holder.getAdapterPosition()).getFileType().equals("pdf")) {
            holder.fileTypeTv.setText("P");
            holder.fileTypeRl.setBackgroundResource(R.drawable.pdf_bg);
        } else if (filesArrayList.get(holder.getAdapterPosition()).getFileType().equals("txt")) {
            holder.fileTypeRl.setBackgroundResource(R.drawable.text_bg);
            holder.fileTypeTv.setText("T");
        } else if (filesArrayList.get(holder.getAdapterPosition()).getFileType().equals("xls") ||
                filesArrayList.get(holder.getAdapterPosition()).getFileType().equals("xlsx")) {
            holder.fileTypeRl.setBackgroundResource(R.drawable.excel_bg);
            holder.fileTypeTv.setText("E");
        } else if (filesArrayList.get(holder.getAdapterPosition()).getFileType().equals("doc") ||
                filesArrayList.get(holder.getAdapterPosition()).getFileType().equals("docx")) {
            holder.fileTypeTv.setText("W");
            holder.fileTypeRl.setBackgroundResource(R.drawable.word_bg);
        }

        //more button Visible or not

        holder.moreImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PopupMenu menu = new PopupMenu(context, holder.fileNameTv, Gravity.END);
                menu.inflate(R.menu.navigation);
                menu.getMenu().findItem(R.id.convertPdf).setVisible(isVisible());
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.rename) {
                            menu.dismiss();
                            showRenameDialog(filesArrayList.get(holder.getAdapterPosition()).getFile(), holder.getAdapterPosition());
                            return true;
                        } else if (menuItem.getItemId() == R.id.share) {
                            shareFile(filesArrayList.get(holder.getAdapterPosition()).getFile());
                            menu.dismiss();
                            return true;
                        } else if (menuItem.getItemId() == R.id.delete) {
                            showDeleteFileDialog(filesArrayList.get(holder.getAdapterPosition()).getFile(), holder.getAdapterPosition());
                            menu.dismiss();
                            return true;
                        } else if (menuItem.getItemId() == R.id.moveToFolder) {
                            showAllFolderDialog(filesArrayList.get(holder.getAdapterPosition()).getFile(), holder.getAdapterPosition());
                            menu.dismiss();
                            return true;
                        } else if (menuItem.getItemId() == R.id.convertPdf) {
                            convertFile(filesArrayList.get(holder.getAdapterPosition()).getFile());
                            menu.dismiss();
                            return true;
                        }
                        return false;
                    }
                });
                menu.show();
            }

        });

        holder.rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(context instanceof SearchPdfFileActivity){
                    Intent intent = new Intent();
                    intent.putExtra("path", filesArrayList.get(holder.getAdapterPosition()).getFile().getAbsolutePath());

                    ((SearchPdfFileActivity) context).setResult(Constants.OPEN_SEARCH_REQUEST_CODE,intent);
                    ((SearchPdfFileActivity) context).finish();

                }else {
                    if (context instanceof MainActivity) {
                        Fragment fragment = ((MainActivity) context).tabsadapter.getItem(((MainActivity) context).viewPager.getCurrentItem());
                        if (fragment instanceof FileFragment) {
                            if (((FileFragment) fragment).isMultiSelect) {
                                holder.selectedItem(filesArrayList.get(holder.getAdapterPosition()));
                                return;
                            }
                        }
                    }
                    try {

                        if (holder.fileTypeTv.getText().toString().equals("E")) {
//                            Constants.excelIntent(context, filesArrayList.get(holder.getAdapterPosition()).getFile());
                            convertFile(filesArrayList.get(holder.getAdapterPosition()).getFile());
                        } else if (holder.fileTypeTv.getText().toString().equals("T")) {
//                            Constants.textFileIntent(context, filesArrayList.get(holder.getAdapterPosition()).getFile());
                            convertFile(filesArrayList.get(holder.getAdapterPosition()).getFile());
                        } else if (holder.fileTypeTv.getText().toString().equals("W")) {
                            convertFile(filesArrayList.get(holder.getAdapterPosition()).getFile());
//                            Constants.doxFileIntent(context, filesArrayList.get(holder.getAdapterPosition()).getFile());
                        } else if (holder.fileTypeTv.getText().toString().equals("P")) {
                            Intent intent = new Intent(context, PDFViewerAcitivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("path", filesArrayList.get(holder.getAdapterPosition()).getFile().getAbsolutePath());
                            context.startActivity(intent);
                        }
                    } catch (Exception e) {
                        Log.d("exxx", "" + e.getMessage());
                    }
                }
            }


        });


        if (holder.checkBox.getVisibility() == View.VISIBLE) {
            if (filesArrayList.get(holder.getAdapterPosition()).getSelect()) {
                holder.checkBox.setChecked(true);
            } else {
                holder.checkBox.setChecked(false);
            }

        }



        if(position==4 && !SharePrefData.getInstance().getADS_PREFS()){

            holder.adlayout.setVisibility(View.VISIBLE);
            if(googlead!=null){
                holder.admobNativeView.setVisibility(View.VISIBLE);
                holder.nativeAdContainer.setVisibility(View.GONE);
                holder.adlayout2.setVisibility(View.GONE);
                populateUnifiedNativeAdView(googlead);
            }else if(fbad!=null){
                holder.admobNativeView.setVisibility(View.GONE);
                holder.nativeAdContainer.setVisibility(View.VISIBLE);
                holder.adlayout2.setVisibility(View.GONE);
                inflateAd(fbad);
            }
        }

    }

    public ArrayList<FileInfoModel> getFilesArrayList() {
        return checkBoxArray;
    }

    public ArrayList<FileInfoModel> getRealArray() {
        return filesArrayList;
    }

    private boolean isVisible() {
        return context instanceof MainActivity && ((MainActivity) context).viewPager.getCurrentItem() == 0;
    }

    private void convertFile(File file) {
        String ext = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
        if (ext.contains(Constants.excelExtension) || ext.contains(Constants.excelWorkbookExtension)) {
            File pdfFile = mDirectory.createExcelToPdf(file);
            if (pdfFile != null) {
//                refreshArray(pdfFile);
                Intent intent = new Intent(context, PDFViewerAcitivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("path", pdfFile.getAbsolutePath());
                context.startActivity(intent);
            }
        } else if (ext.contains(Constants.docExtension) || ext.contains(Constants.docxExtension)
                || ext.contains(Constants.textExtension)) {
            if (callback != null) {
                callback.callback(file);

            }
        }


    }

    public void refreshArray(File pdfFile) {
        String[] names = pdfFile.getName().split("\\.");
        FileInfoModel model = new FileInfoModel(names[0],
                pdfFile.getAbsolutePath().substring(pdfFile.getAbsolutePath().lastIndexOf(".")).replace(".", ""),
                pdfFile, false);
        ArrayList<FileInfoModel> arrayList = filesArrayList;
        arrayList.add(filesArrayList.size(), model);
        setData(arrayList);
        if (recyclerView != null)
            recyclerView.smoothScrollToPosition(filesArrayList.size());
    }

    private void shareFile(File file) {
        Constants.shareFile(context, file);
    }

    private void showDeleteFileDialog(final File file, final int pos) {
        AlertDialogHelper.showAlert(context, new AlertDialogHelper.Callback() {
            @Override
            public void onSucess(int t) {
                if (t == 0) {
                    if (mDirectory.deleteFile(file)) {
                        Toast.makeText(context, "File deleted", Toast.LENGTH_SHORT).show();
                        filesArrayList.remove(pos);
                        notifyItemRemoved(pos);
                    } else {
                        Toast.makeText(context, "File not deleted", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }, "Delete", "Are you really want to delete this file?");
    }

    private void showRenameDialog(final File file, final int pos) {
        CreateFolderDialog dialog = new CreateFolderDialog(context, new GenericCallback() {
            @Override
            public void callback(Object o) {
                if (mDirectory.renameFile(file, (String) o)) {
                    Toast.makeText(context, "File is renamed", Toast.LENGTH_SHORT).show();
                    FileInfoModel model = getItem(pos);
                    model.setFileName((String) o);
                    notifyDataSetChanged();
                } else {
                    Toast.makeText(context, "File renamed failed", Toast.LENGTH_SHORT).show();
                }
            }
        }, false);
        dialog.setSaveBtn("Rename File");
        dialog.setTitle("Enter New File Name");
        dialog.show();
    }

    public void refrechList() {
        for (FileInfoModel model : filesArrayList) {
            model.setSelect(false);
        }
        notifyDataSetChanged();
    }

    public void filterList(ArrayList<FileInfoModel> filterdNames) {
        this.filesArrayList = filterdNames;
        notifyDataSetChanged();
    }

    public FileInfoModel getItem(int position) {
        return filesArrayList.get(position);
    }

    public void showAllFolderDialog(File moveFile, final int pos) {
        new MoveFileDialog(context, moveFile, new GenericCallback() {
            @Override
            public void callback(Object o) {
                if ((boolean) o) {
                    Toast.makeText(context, "File move to folder", Toast.LENGTH_SHORT).show();
                    filesArrayList.remove(pos);
                    notifyItemRemoved(pos);

                    NormalUtils.getInstance().showSuccessDialog(context,"Success");

                } else {
                    Toast.makeText(context, "File not move", Toast.LENGTH_SHORT).show();
                }
            }
        }).show();
    }

    @Override
    public int getItemCount() {
        return filesArrayList.size();
    }

    public class MyFilesHolder extends RecyclerView.ViewHolder {
        RelativeLayout fileTypeRl;
        TextView fileTypeTv, fileNameTv, sizeTv, dateTv;
        ImageView moreImg;
        ConstraintLayout rootLayout;
        CheckBox checkBox;
        FrameLayout admobNativeView;
        NativeAdLayout nativeAdContainer;
        ConstraintLayout adlayout;
        View adlayout2;

        public MyFilesHolder(@NonNull View itemView) {
            super(itemView);

            adlayout2=itemView.findViewById(R.id.adlayout2);
            admobNativeView = itemView.findViewById(R.id.admobNativeView);
            nativeAdContainer = itemView.findViewById(R.id.native_ad_container);
            adlayout=itemView.findViewById(R.id.adlayout);

            fileTypeRl = itemView.findViewById(R.id.fileTypeRl);
            fileTypeTv = itemView.findViewById(R.id.fileTypeTv);
            fileNameTv = itemView.findViewById(R.id.fileNameTv);
            sizeTv = itemView.findViewById(R.id.sizeTv);
            dateTv = itemView.findViewById(R.id.dateTv);
            moreImg = itemView.findViewById(R.id.moreImg);
            rootLayout = itemView.findViewById(R.id.myroot);
            checkBox = itemView.findViewById(R.id.checkbox);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    FileInfoModel model = filesArrayList.get(getAdapterPosition());
                    if (b) {
                        if (model.getSelect())
                            return;
                        else {
                            model.setSelect(true);
                            checkBoxArray.add(model);
                        }

                    } else {
                        if (!model.getSelect()) {
                            return;
                        } else {
                            model.setSelect(false);
                            checkBoxArray.remove(model);
                        }
                    }
                    notifyDataSetChanged();
                }
            });
        }

        public void selectedItem(FileInfoModel model) {
            for (FileInfoModel model1 : filesArrayList) {
                model1.setSelect(model.getFileName().equals(model1.getFileName()));
            }
            notifyDataSetChanged();
        }


        public void setViewsAllignment() {
            ConstraintLayout constraintLayout = rootLayout;
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.connect(R.id.fileNameTv, ConstraintSet.END, R.id.checkbox, ConstraintSet.START, 0);
            constraintSet.connect(R.id.dateTv, ConstraintSet.END, R.id.checkbox, ConstraintSet.START, context.getResources().getDimensionPixelSize(R.dimen._10sdp));
            constraintSet.applyTo(constraintLayout);
            moreImg.setVisibility(View.GONE);
            checkBox.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setAd(UnifiedNativeAd mUnifiedNativeAd, NativeAd mNativeAd){
        if(myFilesHolder!=null && myFilesHolder.adlayout2!=null &&
                myFilesHolder.nativeAdContainer!=null && myFilesHolder.admobNativeView!=null) {
            if (mUnifiedNativeAd != null) {
                googlead = mUnifiedNativeAd;
//                myFilesHolder.adlayout2.setVisibility(View.GONE);
//                myFilesHolder.nativeAdContainer.setVisibility(View.GONE);
//                myFilesHolder.admobNativeView.setVisibility(View.VISIBLE);
//            populateUnifiedNativeAdView(mUnifiedNativeAd);
            } else if (mNativeAd != null) {
                fbad = mNativeAd;
//                myFilesHolder.adlayout2.setVisibility(View.GONE);
//                myFilesHolder.nativeAdContainer.setVisibility(View.VISIBLE);
//                myFilesHolder.admobNativeView.setVisibility(View.GONE);
//            inflateAd(mNativeAd);

            }
        }
        notifyDataSetChanged();
    }

    private void inflateAd(NativeAd nativeAd) {
        nativeAd.unregisterView();

        ConstraintLayout fbAdview =(ConstraintLayout)LayoutInflater.from(context).inflate(R.layout.home_fb_native, myFilesHolder.nativeAdContainer, false);

        myFilesHolder.nativeAdContainer.addView(fbAdview);

        LinearLayout adChoicesContainer = fbAdview.findViewById(R.id.ad_choices_container);
        AdOptionsView adOptionsView = new AdOptionsView(context, nativeAd, myFilesHolder.nativeAdContainer);
        adOptionsView.setIconColor(Color.parseColor("#271337"));
        adChoicesContainer.removeAllViews();
        adChoicesContainer.addView(adOptionsView, 0);


        com.facebook.ads.MediaView nativeAdIcon = fbAdview.findViewById(R.id.native_ad_icon);
        TextView nativeAdTitle = fbAdview.findViewById(R.id.native_ad_title);
        TextView nativeAdBody = fbAdview.findViewById(R.id.native_ad_body);
        TextView sponsoredLabel = fbAdview.findViewById(R.id.native_ad_sponsored_label);
        TextView nativeAdSocialContext = fbAdview.findViewById(R.id.native_ad_social_context);
        Button nativeAdCallToAction = fbAdview.findViewById(R.id.native_ad_call_to_action);
        ConstraintLayout contentsFb = fbAdview.findViewById(R.id.contentfb);

        com.facebook.ads.MediaView nativeAdMedia = fbAdview.findViewById(R.id.native_ad_media);

        nativeAdTitle.setText(nativeAd.getAdvertiserName());
        nativeAdBody.setText(nativeAd.getAdBodyText());
        nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
        if (nativeAd.hasCallToAction()) {
            nativeAdCallToAction.setVisibility(View.VISIBLE);
        } else {
            nativeAdCallToAction.setVisibility(View.INVISIBLE);
        }
        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        sponsoredLabel.setText(nativeAd.getSponsoredTranslation());

        List<View> clickableViews = new ArrayList<>();

        clickableViews.add(nativeAdCallToAction);
        clickableViews.add(nativeAdMedia);
        clickableViews.add(nativeAdIcon);


        nativeAd.registerViewForInteraction(
                fbAdview,
                nativeAdMedia,
                nativeAdIcon,
                clickableViews
        );


    }
    UnifiedNativeAdView adView;
    private void populateUnifiedNativeAdView(UnifiedNativeAd nativeAd) {

        UnifiedNativeAdView adView= (UnifiedNativeAdView)LayoutInflater.from(context).inflate(R.layout.loading_admob_native, null);

        MediaView mediaView = adView.findViewById(R.id.ad_media);
        mediaView.setImageScaleType(ImageView.ScaleType.FIT_XY);
        adView.setMediaView(mediaView);
        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        /* adView.setPriceView(adView.findViewById(R.id.ad_price))
         adView.setStarRatingView(adView.findViewById(R.id.ad_stars))
         adView.setStoreView(adView.findViewById(R.id.ad_store))
         adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser))*/
        // The headline is guaranteed to be in every UnifiedNativeAd.

        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }
        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }
        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable()
            );
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        adView.setNativeAd(nativeAd);

        myFilesHolder.admobNativeView.removeAllViews();
        myFilesHolder.admobNativeView.addView(adView);

    }

}
