package com.coolweather.app.activity;

import java.util.*;

import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.*;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import android.R;
import android.app.*;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.*;
import android.widget.AdapterView.*;

public class ChooseAreaActivity extends Activity {
	
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB cwDB;
	private List<String> dataList = new ArrayList<String>();
	
	//area list
	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;
	
	private Province selectedProvince;
	private City selectedCity;
	private County selectedCounty;
	
	private int currentLevel;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature( Window.FEATURE_NO_TITLE);
		SetContenView(R.layout.choose_area);
		listView = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		cwDB = CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index,
					long arg3) {
				// TODO Auto-generated method stub
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(index);
					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(index);
					queryCounties();
				}
			}			
		});
		queryProvinces();
	}
	
	//query provinces, local first, then server side
	private void queryProvinces(){
		provinceList = cwDB.loadProvinces();
		if (provinceList.size() > 0) {
			dataList.clear();
			for (Province province : provinceList) {
				dataList.add( province.getProvinceName() );
			}
			
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("China");
			currentLevel = LEVEL_PROVINCE;			
		} else 
			queryFromServer(null, "province");
	}
	
	//query cities, local first, then server side
	private void queryCities(){
		cityList = cwDB.loadCities(selectedProvince.getId());
		if (cityList.size() > 0) {
			dataList.clear();
			for (City item : cityList) {
				dataList.add( item.getCityName() );
			}
			
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText( selectedProvince.getProvinceName() );
			currentLevel = LEVEL_CITY;			
		} else 
			queryFromServer(selectedProvince.getProvinceCode(), "city");
	}
	
	//query cities, local first, then server side
	private void queryCounties(){
		countyList = cwDB.loadCounties(selectedCity.getId());
		if (countyList.size() > 0) {
			dataList.clear();
			for (County item : countyList) {
				dataList.add( item.getCountyName() );
			}
			
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText( selectedCity.getCityName() );
			currentLevel = LEVEL_COUNTY;			
		} else 
			queryFromServer(selectedCity.getCityCode(), "county");
	}		
	
	private void queryFromServer(final String code, final String type) {
		String address;
		if (!TextUtils.isEmpty(code)) 
			address = "http://www.weather.com.cn/data/list3/city" + code +
					".xml";
		else
			address = "http://www.weather.com.cn/data/list3/city.xml";
		
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				boolean res = false;
				if ("provice".equals(type))
					res = Utility.handleProvincesResponse(cwDB, response);
				else if ("city".equals(type))
					res = Utility.handleCitiesResponse(cwDB, response,
							selectedProvince.getId());
				else if ("county".equals(type))
					res = Utility.handleCountiesResponse(cwDB, response,
							selectedCity.getId());
				
				if (res) {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							closeProgressDialog();
							if ("province".equals(type))
								queryProvinces();
							else if ("city".equals(type))
								queryCities();
							else if ("county".equals(type))
								queryCounties();
						}						
					});
				}
			}

			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				runOnUiThread( new Runnable(){
					@Override
					public void run() {
						// TODO Auto-generated method stub
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, 
								"Load failed", Toast.LENGTH_SHORT).show();
					}					
				});
			}
			
		});
	}
	
	//open progress dialog
	private void showProgressDialog() {
		// TODO Auto-generated method stub
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("Loading...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	//cloase progress dialog
	private void closeProgressDialog() {
		// TODO Auto-generated method stub
		if (progressDialog != null)
			progressDialog.dismiss();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (currentLevel == LEVEL_COUNTY)
			queryCities();
		else if (currentLevel == LEVEL_CITY)
			queryProvinces();
		else
			finish();
	}	
	
}