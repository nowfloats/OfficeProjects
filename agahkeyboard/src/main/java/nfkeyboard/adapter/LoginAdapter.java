package nfkeyboard.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import nfkeyboard.interface_contracts.ItemClickListener;
import nfkeyboard.models.AllSuggestionModel;
import nfkeyboard.util.MethodUtils;

/**
 * Created by Admin on 01-03-2018.
 */

public class LoginAdapter extends BaseAdapter<AllSuggestionModel> {

    LoginAdapter(Context context, ItemClickListener listener) {
        super(context, listener);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(nowfloats.nfkeyboard.R.layout.adapter_item_text, parent, false);
        return new LoginHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, AllSuggestionModel suggestion) {
        if (holder instanceof LoginHolder) {
            LoginHolder myHolder = (LoginHolder) holder;
            myHolder.setModelData(suggestion);
        }
    }

    class LoginHolder extends RecyclerView.ViewHolder {
        TextView suggestionTv;
        AllSuggestionModel dataModel;

        public LoginHolder(View itemView) {
            super(itemView);
            linLayoutParams.setMargins(metrics.widthPixels * 12 / 100, topSpace, 0, topSpace);
            itemView.setLayoutParams(linLayoutParams);
            suggestionTv = itemView.findViewById(nowfloats.nfkeyboard.R.id.textView);
            suggestionTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // start Activity login
                    MethodUtils.startBoostActivity(mContext);
                }
            });
        }

        void setModelData(AllSuggestionModel model) {
            dataModel = model;
            suggestionTv.setText(model.getText());
            suggestionTv.setTextSize(16);
            suggestionTv.setAllCaps(true);
            suggestionTv.setTextColor(ContextCompat.getColor(mContext, nowfloats.nfkeyboard.R.color.white));
            suggestionTv.setPadding(2 * leftSpace, leftSpace, 2 * leftSpace, leftSpace);
            suggestionTv.setBackgroundResource(nowfloats.nfkeyboard.R.drawable.yellow_button_bg);
        }
    }
}