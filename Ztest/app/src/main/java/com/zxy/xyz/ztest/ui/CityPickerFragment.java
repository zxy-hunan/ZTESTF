package com.zxy.xyz.ztest.ui;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.lljjcoder.citypickerview.widget.CityPicker;
import com.zxy.xyz.ztest.R;
import com.zxy.xyz.ztest.adapter.CityAdapter;
import com.zxy.xyz.ztest.biz.City;
import com.zxy.xyz.ztest.biz.WeatherInfo;
import com.zxy.xyz.ztest.database.SqliteDBUtil;
import com.zxy.xyz.ztest.util.WeatherJsonUtil;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by 51c on 2017/4/21.
 */

public class CityPickerFragment extends Fragment implements View.OnClickListener{
    private static String APPKEY = "1d17e4da746dc";


    private View view;
    private GridView grid_view;
    private ImageView image_setting,image_addcity,image_return;
    private SqliteDBUtil sqliteDBUtil;
    private CityAdapter cityAdapter;
    private ArrayList<WeatherInfo> al=new ArrayList<>();

    public CityPickerFragment() throws UnsupportedEncodingException {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        x.view().inject(getActivity());
        view=inflater.inflate(R.layout.citypickerfragment,null);
        initView();
        sqliteDBUtil=new SqliteDBUtil(getActivity());
        return view;
    }

    private void initView() {

        grid_view=(GridView)view.findViewById(R.id.grid_view);
        image_setting = (ImageView) view.findViewById(R.id.image_setting);
        image_addcity = (ImageView) view.findViewById(R.id.image_addcity);
        image_return = (ImageView) view.findViewById(R.id.image_return);
        image_setting.setOnClickListener(this);
        image_addcity.setOnClickListener(this);
        image_return.setOnClickListener(this);
    }


    @Override
    public void onResume() {
        super.onResume();
        final ArrayList<City> arrayList = sqliteDBUtil.getCityList();
        if(arrayList.size()!=0) {

            for (int i=0;i<arrayList.size();i++){
                City c=arrayList.get(i);
                String city = c.getDistrict();
                String province=c.getProvince();
                String s=province.substring(0,province.length()-1);
                String CITY = null;
                String PROVINCE = null;
                try {
                    CITY = URLEncoder.encode(city, "UTF-8");
                    PROVINCE =URLEncoder.encode(s, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String WEATHERURI = "http://apicloud.mob.com/v1/weather/query?key=" + APPKEY + "&city=" + CITY + "&province=" + PROVINCE;
                RequestParams params = new RequestParams(WEATHERURI);
                x.http().get(params, new Callback.CommonCallback<String>() {
                    @Override
                    public void onSuccess(String s) {
                       WeatherInfo wi= WeatherJsonUtil.getWeaInfo(s);
                        HashMap<String,Object> map=new HashMap<String, Object>();
                        map.put("wi",wi);
                        map.put("al",arrayList);
                        Message msg=new Message();
                        msg.what=1;
                        msg.obj=map;
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void onError(Throwable throwable, boolean b) {

                    }

                    @Override
                    public void onCancelled(CancelledException e) {

                    }

                    @Override
                    public void onFinished() {

                    }
                });
            }

            cityAdapter = new CityAdapter(getActivity(), arrayList,al);
            grid_view.setAdapter(cityAdapter);
    }


    }
    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==1){
                HashMap map=(HashMap)msg.obj;
                WeatherInfo wi=(WeatherInfo) map.get("wi");
                ArrayList<City> aList=(ArrayList<City>)map.get("al");
                al.add(wi);
                if(al.size()==aList.size()){

                }
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case  R.id.grid_view:

                break;
            case  R.id.image_setting:

                break;
            case  R.id.image_addcity:
                CityPicker cityPicker = new CityPicker.Builder(getActivity())
                        .textSize(20)
                        .title("地址选择")
                        .backgroundPop(0xa0000000)
                        .titleBackgroundColor("#234Dfa")
                        .titleTextColor("#000000")
                        .backgroundPop(0xa0000000)
                        .confirTextColor("#000000")
                        .cancelTextColor("#000000")
                        .province("广东省")
                        .city("深圳市")
                        .district("宝安区")
                        .textColor(Color.parseColor("#000000"))
                        .provinceCyclic(true)
                        .cityCyclic(false)
                        .districtCyclic(false)
                        .visibleItemsCount(7)
                        .itemPadding(10)
                        .onlyShowProvinceAndCity(false)
                        .build();
                cityPicker.show();

                //监听方法，获取选择结果
                cityPicker.setOnCityItemClickListener(new CityPicker.OnCityItemClickListener() {
                    @Override
                    public void onSelected(String... citySelected) {
                        //省份
                        String province = citySelected[0];
                        //城市
                        String city = citySelected[1];
                        //区县（如果设定了两级联动，那么该项返回空）
                        String district = citySelected[2];
                        //邮编
                        String code = citySelected[3];
                        int result=sqliteDBUtil.insertoDB(province,city,district,code);
                        Toast.makeText(getActivity(),""+province+city+district+code,Toast.LENGTH_SHORT).show();
//                        cityAdapter.notifyDataSetChanged();
                        grid_view.setAdapter(cityAdapter);

                    }

                    @Override
                    public void onCancel() {
                    }
                });
                break;
            case  R.id.image_return:
                try {
                    getActivity().getFragmentManager().beginTransaction().replace(R.id.content_main,new WeatherFragment()).commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

        }
    }
}
