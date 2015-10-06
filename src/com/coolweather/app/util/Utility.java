package com.coolweather.app.util;

import android.text.TextUtils;

import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.*;

public class Utility {
	//parse and process province data from server
	public synchronized static boolean handleProvincesResponse(
			CoolWeatherDB cwDB, String res) {
		if (!TextUtils.isEmpty(res)) {
			String[] allProvinces = res.split(",");
			if (allProvinces != null && allProvinces.length > 0) {
				for (String p : allProvinces) {
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceCode( array[0] );
					province.setProvinceName( array[1] );
					cwDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}
	
	//parse and process city data from server
	public synchronized static boolean handleCitiesResponse(
			CoolWeatherDB cwDB, String res, int provinceId) {
		if (!TextUtils.isEmpty(res)) {
			String[] allCities = res.split(",");
			if (allCities != null && allCities.length > 0) {
				for (String c : allCities) {
					String[] array = c.split("\\|");
					City city = new City();
					city.setCityCode( array[0] );
					city.setCityName( array[1] );
					city.setProvinceId(provinceId);
					cwDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}
	
	//parse and process county data from server
	public synchronized static boolean handleCountiesResponse(
			CoolWeatherDB cwDB, String res, int cityId) {
		if (!TextUtils.isEmpty(res)) {
			String[] allCounties = res.split(",");
			if (allCounties != null && allCounties.length > 0) {
				for (String c : allCounties) {
					String[] array = c.split("\\|");
					County county = new County();
					county.setCountyCode( array[0] );
					county.setCountyName( array[1] );
					county.setCityid(cityId);
					cwDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}	
}
