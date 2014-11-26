package com.example.earth.testgooglemap;

import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by EARTH on 26/11/2557.
 */
public class Requesttask extends AsyncTask<String, String, String> {
    private GoogleMap mMap;

    public Requesttask(GoogleMap mMap){
        this.mMap = mMap;
    }

    @Override
    protected String doInBackground(String... uri) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;
        try {
            response = httpclient.execute(new HttpGet(uri[0]));
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
            } else{
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
            //TODO Handle problems..
        } catch (IOException e) {
            //TODO Handle problems..
        }

        return responseString;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        setBusLocation(parseXmlToBus(result));
//        System.out.println(parseXmlToBus(result).toString());
    }

    public static List<Bus> parseXmlToBus(String inputXml)
    {
        inputXml = inputXml.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>", "");
        inputXml = inputXml.replace("<buses>", "").replace("</buses>", "");
        String[] temp = inputXml.split("</bus>");
        List<Bus> busList = new ArrayList<Bus>();
        for (int i=0; i<temp.length; i++)
        {
            Bus bus = new Bus();
            String[] temp2 = temp[i].split(">");
            bus.setId(Long.parseLong(temp2[0].split("\"")[1]));
            bus.setBusLineID((Integer.parseInt(temp2[2].split("<")[0])));
            bus.setLat((Double.parseDouble(temp2[4].split("<")[0])));
            bus.setLon(Double.parseDouble(temp2[6].split("<")[0]));
            bus.setTimestamp(temp2[8].split("<")[0]);
            busList.add(bus);
        }
        return busList;
    }

    public void setBusLocation(List<Bus> buses){

        for(Bus b : buses){
            if(b != null) {
                MarkerOptions mo = new MarkerOptions();
                mo.title(b.getBusLineID()+"");
                mo.visible(true);
                mMap.addMarker(mo.position(new LatLng(b.getLat(), b.getLon())));
            }

        }


    }

}
