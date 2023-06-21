package com.akw.crimson.Registration;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.R;


public class Registration_Phone extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String phoneNumber;

    private TextView tv_countryCode;
    private EditText et_phoneNumber;
    private Button b_next;
    private Spinner spinner;

    final private String[] countryDialCodes ={"91","93","355","213","1-684","376","244","1-264","672","1-268","54","374","297","61","43","994","1-242","973","880","1-246","375","32","501","229","1-441","975","591","387","267","55","673","359","226","257","855","237","1","238","1-345","236","235","56","86","53","61","57","269","243","242","682","506","225","385","53","357","420","45","253","1-767","1-809","670","593","20","503","240","291","372","251","500","298","679","358","33","594","689","241","220","995","49","233","350","30","299","1-473","590","1-671","502","224","245","592","509","504","852","36","354","91","62","98","964","353","972","39","1-876","81","962","7","254"
            ,"686","850","82","965","996","856","371","961","266","231","218","423","370","352","853","389","261","265","60","960","223","356","692","596","222","230","269","52","691","373","377","976","1-664","212","258","95","264","674","977","31","599","687","64","505","227","234","683","672","1-670","47","968","92","680","970","507","675","595","51","63","48","351","1-787 or 1-939","974","262","40","7","250","290","1-869","1-758","508","1-784","685","378","239","966","221","248","232","65","421","386","677","252","27","34","94","249","597","268","46","41","963","886","992","255","66","690","676","1-868","216","90","993","1-649","688","256","380"
            ,"971","44","1","598","998","678","418","58","84","1-284","1-340","681","967","260","263"};

    final private String[] countryList= {"Select a Country","Afghanistan","Albania","Algeria","American Samoa","Andorra, Principality of ","Angola","Anguilla","Antarctica","Antigua and Barbuda","Argentina","Armenia","Aruba","Australia","Austria","Azerbaijan or Azerbaidjan ","Bahamas, Commonwealth of The","Bahrain, Kingdom of ","Bangladesh ","Barbados","Belarus","Belgium","Belize","Benin ","Bermuda","Bhutan, Kingdom of","Bolivia","Bosnia and Herzegovina","Botswana ","Brazil","Brunei ","Bulgaria","Burkina Faso ","Burundi ","Cambodia","Cameroon","Canada","Cape Verde","Cayman Islands","Central African Republic","Chad","Chile","China","Christmas Island","Cocos (Keeling) Islands","Colombia"
            ,"Comoros, Union of the ","Congo, Democratic Republic of the","Congo, Republic of the","Cook Islands ","Costa Rica","Cote D'Ivoire","Croatia (Hrvatska)","Cuba","Cyprus","Czech Republic","Denmark","Djibouti ","Dominica","Dominican Republic","East Timor","Ecuador","Egypt ","El Salvador","Equatorial Guinea ","Eritrea ","Estonia","Ethiopia","Falkland Islands (Islas Malvinas)","Faroe Islands","Fiji","Finland","France","French Guiana or French Guyana","French Polynesia ","Gabon (Gabonese Republic)","Gambia","Georgia","Germany","Ghana ","Gibraltar","Greece","Greenland","Grenada","Guadeloupe","Guam","Guatemala","Guinea ","Guinea-Bissau","Guyana "
            ,"Haiti","Honduras","Hong Kong","Hungary","Iceland","India","Indonesia ","Iran","Iraq","Ireland","Israel","Italy","Jamaica","Japan","Jordan ","Kazakstan or Kazakhstan ","Kenya ","Kiribati ","North Korea","South Korea","Kuwait","Kyrgyzstan","Laos","Latvia ","Lebanon","Lesotho ","Liberia","Libya (Libyan Arab Jamahiriya)","Liechtenstein","Lithuania ","Luxembourg","Macau","Macedonia, ","Madagascar","Malawi ","Malaysia","Maldives","Mali ","Malta","Marshall Islands ","Martinique (French)","Mauritania","Mauritius","Mayotte ","Mexico","Micronesia","Moldova","Monaco","Mongolia","Montserrat","Morocco","Mozambique ","Myanmar","Namibia ","Nauru"
            ,"Nepal","Netherlands","Netherlands Antilles","New Caledonia","New Zealand (Aotearoa)","Nicaragua","Niger","Nigeria","Niue ","Norfolk Island","Northern Mariana Islands","Norway","Oman","Pakistan","Palau","Palestinian State (Proposed)","Panama","Papua New Guinea ","Paraguay","Peru","Philippines","Poland","Portugal","Puerto Rico","Qatar ","Bourbon Island","Romania","Russian Federation","Rwanda","Saint Helena","Saint Kitts and Nevis","Saint Lucia","Saint Pierre and Miquelon","Saint Vincent and the Grenadines","Samoa","San Marino","Sao Tome and Principe","Saudi Arabia","Senegal","Seychelles","Sierra Leone","Singapore","Slovakia","Slovenia"
            ,"Solomon Islands ","Somalia ","South Africa","Spain","Sri Lanka ","Sudan ","Suriname ","Swaziland ","Sweden","Switzerland","Syria ","Taiwan ","Tajikistan ","Tanzania","Thailand","Tokelau","Tonga","Trinidad and Tobago","Tunisia","Turkey","Turkmenistan","Turks and Caicos Islands","Tuvalu ","Uganda","Ukraine ","United Arab Emirates","United Kingdom ","United States","Uruguay","Uzbekistan ","Vanuatu ","Vatican City State ","Venezuela","Vietnam","Virgin Islands, British ","Virgin Islands, United States ","Wallis and Futuna Islands","Yemen","Zambia ","Zimbabwe"};

//    private RequestQueue queue;

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        tv_countryCode.setText(" + " + countryDialCodes[i]+" ");
        et_phoneNumber.setText("");
        phoneNumber= " + " + countryDialCodes[i];
        spinner.setSelection(i);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_phone);

        if(this.getSharedPreferences(SharedPrefManager.SHARED_PREF_NAME, Context.MODE_PRIVATE).getBoolean("PHONE_VERIFIED", false)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                startActivity(new Intent(this, Registration_PublicProfile.class));
            }
            finish();
        }

        setViews();
    }

    private void setViews() {
        spinner= findViewById(R.id.Registration_Phone_Spinner_CountryNames);
        tv_countryCode=findViewById(R.id.Registration_CountryCode_TextViewPhone);
        et_phoneNumber=findViewById(R.id.Registration_Phone_editTextPhone);
        b_next=findViewById(R.id.Registration_Phone_Button_Next);

        b_next.setOnClickListener(view -> {
            if(et_phoneNumber.getText().toString().matches("\\d+")){
                phoneNumber=phoneNumber+et_phoneNumber.getText().toString();

                Intent intent= new Intent(getApplicationContext(), Registration_PhoneVerification.class);

                intent.putExtra(Constants.Intent.KEY_INTENT_PHONE, phoneNumber);


//                    makeTheCall(phoneNumber);

                startActivity(intent);
            }else{
                Toast.makeText(getApplicationContext()," Invalid Number ", Toast.LENGTH_SHORT).show();
                et_phoneNumber.setText("");
            }
        });

        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

    }
}
