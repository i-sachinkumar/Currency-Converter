package com.ihrsachin.currencyandunitconverter


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.*
import android.widget.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.URL
import java.net.URLConnection
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {


    // parameters
    val baseUrl = "https://api.frankfurter.app/"
    var fromCurrency = "AUD"
    var toCurrency = "AUD"
    var exchangeRates: Double = 0.00
    var fromAmount = 1.00
    var toAmount = 0.00


    //from-country widgets
    lateinit var from_country_spinner : Spinner
    lateinit var from_currency_spinner : Spinner
    lateinit var from_amount_editText : EditText

    //to-country widgets
    lateinit var to_country_spinner : Spinner
    lateinit var to_currency_spinner : Spinner
    lateinit var to_amount_editText : EditText


    // date section
    lateinit var date_day_spinner : Spinner
    lateinit var date_month_spinner : Spinner
    lateinit var date_year_spinner : Spinner

    // decimal formatter
    lateinit var decimalFormatter : DecimalFormat

    // arrays of countries and currencies
    lateinit var country_name_array : Array<String>
    lateinit var currency_symbol_array : Array<String>

    //progress bar
    lateinit var progress_circular : ProgressBar

    lateinit var whole_thing : LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)






        /*********** initializing *********************************/

        //from-country widgets
        from_country_spinner = findViewById(R.id.from_country_spinner)
        from_currency_spinner = findViewById(R.id.from_currency_spinner)
        from_amount_editText = findViewById(R.id.from_amount_editText)


        //to-country widgets
        to_country_spinner = findViewById(R.id.to_country_spinner)
        to_currency_spinner = findViewById(R.id.to_currency_spinner)
        to_amount_editText = findViewById(R.id.to_amount_editText)


        //date section
        date_day_spinner = findViewById(R.id.date_day_spinner)
        date_month_spinner = findViewById(R.id.date_month_spinner)
        date_year_spinner = findViewById(R.id.date_year_spinner)

        // arrays of countries and currencies
        country_name_array = resources.getStringArray(R.array.country_name_array)
        currency_symbol_array = resources.getStringArray(R.array.country_symbol_array)

        decimalFormatter = DecimalFormat("#.##")
        progress_circular = findViewById(R.id.progress_circular)

        whole_thing = findViewById(R.id.whole_thing)


        from_amount_editText.text.append("1")

        /** Spinning spinner for both from and to countries **/
        ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            country_name_array
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            from_country_spinner.adapter = adapter
            to_country_spinner.adapter = adapter
        }

        /** Spinning spinner for both from and to currencies **/
        ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            currency_symbol_array
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            from_currency_spinner.adapter = adapter
            to_currency_spinner.adapter = adapter
        }

        /** If spinning a country spinner,
         ** then it's corresponding currency spinner should spin automatically  and vice versa
         ** also update the current to and from currency symbol **/

        //from-section
        from_country_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                from_currency_spinner.setSelection(position)
                fromCurrency = currency_symbol_array[position]
                updateOnMainThread()
            }

        }

        from_currency_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                from_country_spinner.setSelection(position)
                fromCurrency = currency_symbol_array[position]

            }

        }

        //to-section
        to_country_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                to_currency_spinner.setSelection(position)
                toCurrency = currency_symbol_array[position]
                updateOnMainThread()
            }

        }
        to_currency_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                to_country_spinner.setSelection(position)
                toCurrency = currency_symbol_array[position]

            }

        }


        from_amount_editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                toAmount = charSequence.toString().toDouble()
            }

            override fun afterTextChanged(editable: Editable) {}
        })


//    fun updateToAmount(){
//
//        try {
//            if (from_amount_editText.text.isNotBlank() && from_amount_editText.text.isNotEmpty()) {
//                fromAmount = from_amount_editText.text.toString().toDouble()
//                toAmount = decimalFormatter.format(fromAmount * exchangeRates).toDouble()
//                to_amount_editText.text.clear()
//                to_amount_editText.append(toAmount.toString())
//            }
//        } catch (e : NumberFormatException){
//            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
//        }
//    }


    }

    fun updateOnMainThread(){
        progress_circular.visibility = VISIBLE
        whole_thing.visibility = GONE
        CoroutineScope(IO).launch { apiRequest() }
    }



    private suspend fun passResultOnMainThread(result: Double) {
        withContext (Main) {
            //TODO
            progress_circular.visibility = INVISIBLE
            whole_thing.visibility = VISIBLE
            exchangeRates = result
            to_amount_editText.text.clear()
            to_amount_editText.text.append("${result*from_amount_editText.text.toString().toDouble()}")
            //Toast.makeText(this@MainActivity, "$result", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun apiRequest() {
        val result = getExchangeRateFromApi() // wait until job is done
        passResultOnMainThread(result)
    }


    private suspend fun getExchangeRateFromApi(): Double {
        val url = "${baseUrl}latest?amount=1&from=$fromCurrency&to=$toCurrency"
        var inputLine: String? = ""
        var result = 0.0
        if(toCurrency != fromCurrency) {
            try {
                val website = URL(url)
                val connection: URLConnection = website.openConnection()
                val `in` = BufferedReader(
                    InputStreamReader(
                        connection.getInputStream()))

                inputLine = `in`.readLine()

                `in`.close()
            } catch (e: Exception) {
                withContext(Main) {
                    Toast.makeText(this@MainActivity,
                        "Error! make sure you're connected to internet",
                        Toast.LENGTH_SHORT).show()
                }
            }

            try {
                val json = JSONObject(inputLine!!)
                result = json.getJSONObject("rates").getDouble(toCurrency)
            } catch (e: Exception) {
                //Toast.makeText(context, "Result not found for this combination", Toast.LENGTH_SHORT).show()
            }
        } else{
            result = 1.0
        }
        return result
    }

}