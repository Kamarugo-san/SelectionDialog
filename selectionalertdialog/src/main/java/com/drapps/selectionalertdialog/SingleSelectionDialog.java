package com.drapps.selectionalertdialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SingleSelectionDialog extends AppCompatActivity {
    /**
     * When {@link #returnToAddSearch} is true and the user presses return, this is the index that
     * will be sent in {@link #singleSelectionListener}'s onDialogItemSelected.
     * Use this to know whether the user has pressed return to add the text as selected item.
     */
    public static int RETURN_TO_ADD_SEARCH_ITEM_INDEX = -1;

    Dialog dialog;
    private Context context;
    private ArrayList<String> list = new ArrayList<>();
    private ArrayList<String> temp_data_list = new ArrayList<>();
    private String headerTitle = "Select";
    private Boolean isSearchEnabled = false;
    SingleSelectionAdapter dialogAdapter;
    private String currentField = "", currentValue = "", currentPosition = "", tag = "", hintText = "Search here";
    private int headerColor, textColor;
    SingleSelectionListener singleSelectionListener;
    private boolean returnToAddSearch = false;
    private String returnToAddSearchText = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_selection_dialog);
    }

    private SingleSelectionDialog(Builder builder) {
        context = builder.context;
        list = builder.list;
        temp_data_list.clear();
        temp_data_list = builder.list;
        headerTitle = builder.headerTitle;
        isSearchEnabled = builder.isSearchEnabled;
        tag = builder.tag;
        hintText = builder.hintText;
        currentField = builder.currentField;
        headerColor = builder.headerColor;
        textColor = builder.textColor;
        singleSelectionListener = builder.singleSelectionListener;
        returnToAddSearch = builder.returnToAddSearch;
        returnToAddSearchText = builder.returnToAddSearchText;
        Log.d("TAG--", headerTitle);
    }

    public void show() {
        //Custom pop up dialog for selecting options
        try {
            dialog = new Dialog(context);
            final View convertView = LayoutInflater.from(context).inflate(R.layout.single_selection_dialog, null);
            dialog.setContentView(convertView);
            TextView tvTitle = convertView.findViewById(R.id.tv_title);
            ImageView imgCancel = convertView.findViewById(R.id.img_cancel_single_dialog);
            final RecyclerView recyclerView = convertView.findViewById(R.id.recycler_single_selection);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(layoutManager);
            LinearLayout header = convertView.findViewById(R.id.linear_single_dialog);
            final EditText etSearch = convertView.findViewById(R.id.et_search_single_selection);
            tvTitle.setText(headerTitle);
            final AppCompatTextView tvMessage = convertView.findViewById(R.id.tv_list_message);

            if (isSearchEnabled) {
                etSearch.setVisibility(View.VISIBLE);

                // Return to add search bar text as selected item
                if (returnToAddSearch) {
                    tvMessage.setText(returnToAddSearchText);

                    etSearch.setOnKeyListener(new View.OnKeyListener() {
                        @Override
                        public boolean onKey(View view, int keyCode, KeyEvent event) {
                            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                                if (etSearch.getText() != null) {
                                    final String searchedText = etSearch.getText().toString();
                                    if (!searchedText.isEmpty()) {
                                        dialog.dismiss();

                                        currentValue = searchedText;
                                        currentField = searchedText;
                                        currentPosition = String.valueOf(RETURN_TO_ADD_SEARCH_ITEM_INDEX);

                                        if (singleSelectionListener != null) {
                                            singleSelectionListener.onDialogItemSelected(currentValue, RETURN_TO_ADD_SEARCH_ITEM_INDEX, tag);
                                        }
                                    }
                                }

                                return true;
                            }

                            return false;
                        }
                    });
                }
            } else {
                etSearch.setVisibility(View.GONE);
            }

            if (headerColor != 0) {
                try {
                    header.setBackgroundColor(headerColor);
                    ColorStateList colorStateList = ColorStateList.valueOf(headerColor);
                    ViewCompat.setBackgroundTintList(etSearch, colorStateList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (hintText != null && !hintText.equals("")) {
                etSearch.setHint(hintText);
            }

            imgCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            recyclerView.addOnItemTouchListener(
                    new RecyclerItemClickListener(context, recyclerView,
                            new RecyclerItemClickListener.OnItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {
                                    dialog.dismiss();
                                    currentValue = temp_data_list.get(position);
                                    currentField = getCurrentField(list.get(position));
                                    currentPosition = getCurrentPosition(currentValue);
                                    if (singleSelectionListener != null) {
                                        singleSelectionListener.onDialogItemSelected(currentValue, Integer.parseInt(currentPosition), tag);
                                    }
                                }

                                @Override
                                public void onItemLongClick(View view, int position) {
                                    dialog.dismiss();
                                }

                            })
            );

            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    if (etSearch.getText().toString().equals("")) {
                        if (list != null && list.size() > 0) {
                            dialogAdapter = new SingleSelectionAdapter(singleSelectionListener, list, context, tag, currentField, headerColor, textColor);
                            dialogAdapter.notifyDataSetChanged();
                            recyclerView.setAdapter(dialogAdapter);
                        } else {
                            if (singleSelectionListener != null) {

                                singleSelectionListener.onDialogError("List is empty or null", tag);
                            }
                        }


                    } else {
                        getSearch(etSearch.getText().toString(), recyclerView, tvMessage);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            if (list != null && list.size() > 0) {
                dialogAdapter = new SingleSelectionAdapter(singleSelectionListener, list, context, tag, currentField, headerColor, textColor);
                recyclerView.setAdapter(dialogAdapter);
                dialog.show();
            } else {
                Toast.makeText(context, "List is empty", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getSearch(String search, RecyclerView recyclerView, AppCompatTextView tvMessage) {
        ArrayList<String> temp_list = new ArrayList<>();
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).toLowerCase().contains(search.toLowerCase())) {
                    temp_list.add(list.get(i));
                }
            }
        } else {
            if (singleSelectionListener != null) {
                singleSelectionListener.onDialogError("List is empty or null", tag);
            }
        }

        if (temp_list.size() == 0) {
            tvMessage.setVisibility(View.VISIBLE);
        } else {
            tvMessage.setVisibility(View.GONE);
        }

        temp_data_list = new ArrayList<>();
        temp_data_list.addAll(temp_list);
        dialogAdapter = new SingleSelectionAdapter(singleSelectionListener, temp_data_list, context, tag, currentField, headerColor, textColor);

        dialogAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(dialogAdapter);
    }

    public String getCurrentField(String field) {
        if (list != null && list.size() > 0) {

            for (int i = 0; i < list.size(); i++) {
                if (field.equals(list.get(i))) {
                    return list.get(i);
                }
            }
        }

        return "";
    }

    public void setSelectedField(String selectedField) {
        currentField = selectedField;
    }

    public void dismiss() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }


    public String getCurrentPosition(String field) {
        if (list != null && list.size() > 0) {

            for (int i = 0; i < list.size(); i++) {
                if (field.equals(list.get(i))) {
                    return String.valueOf(i);
                }
            }
        }

        return "";
    }

    public static class Builder {
        //optional
        private Context context;
        private ArrayList<String> list = new ArrayList<>();
        private String headerTitle = "Select";
        private Boolean isSearchEnabled = false;
        private String currentField = "", currentValue = "", currentPosition = "", tag = "", hintText = "Search here";
        private int headerColor, textColor;
        SingleSelectionListener singleSelectionListener;
        int style;

        /**
         * If true, allows user to use the text in the search bar as a selected item by presssing
         * return on their keyboard.
         */
        private boolean returnToAddSearch;

        /**
         * Text to be shown as guide to the user when pressing return to add the text in the search
         * bar as a selected item.
         */
        private String returnToAddSearchText = "Press return to use your search";

        public Builder(Context ctx, String tag) {
            this.context = ctx;
            this.tag = tag;
        }

        public Builder setContent(ArrayList<String> contentProvide) {
            this.list = contentProvide;
            return this;
        }

        public Builder setTitle(String value) {
            if (value != null && !value.equals("")) {
                this.headerTitle = value;
            } else {
                this.headerTitle = "Select";
            }
            return this;
        }

        public Builder setTextColor(int color) {
            this.textColor = color;
            return this;
        }

        public Builder enableSearch(Boolean value, String hint) {
            this.isSearchEnabled = value;
            this.hintText = hint;
            return this;
        }

        public Builder setColor(int color) {
            headerColor = color;
            return this;
        }

        public Builder setListener(SingleSelectionListener listener) {
            singleSelectionListener = listener;
            return this;
        }

        public Builder setSelectedField(String selectedField) {
            currentField = selectedField;
            return this;
        }

        public Builder setReturnToAddSearch(boolean value) {
            returnToAddSearch = value;
            return this;
        }

        public Builder setReturnToAddSearchText(String value) {
            returnToAddSearchText = value;
            return this;
        }

        public SingleSelectionDialog build() {
            return new SingleSelectionDialog(this);
        }
    }

}
