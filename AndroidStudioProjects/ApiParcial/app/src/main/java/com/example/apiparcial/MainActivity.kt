package com.example.apiparcial

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apiparcial.databinding.ActivityMainBinding
import com.example.apiparcial.model.ApiResponse
import com.example.apiparcial.model.Personaje
import com.example.apiparcial.service.RetrofitClient
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListener()
    }



    //Function that listens for an event, for example when clicking on search
    private fun setupClickListener() {

        binding.btnSearch.setOnClickListener {

            // characterInput gets what the user types
            val characterInput = binding.tieCharacterInput.text.toString().trim()


            // if the user presses the search button and there is something written in the input, the body of the if is executed,
         // if the user presses search and there is nothing written in the input, the else that contains the message is executed
            if (characterInput.isEmpty() == false) {

                val characterId = characterInput.toIntOrNull()
                if (characterId != null) {

                    searchCharacterById(characterInput)
                }
                else {
                    searchCharacterByName(characterInput)
                }
            }
            else {
                Toast.makeText(this, "Por favor, ingresa un ID o nombre", Toast.LENGTH_LONG).show()
            }
        }
    }



    // function that searches for a character by its ID
       private fun searchCharacterById(characterId: String) {

        showLoading(true)

          // call the API with Retrofit
           RetrofitClient.rickMortyApiService.getCharacterById(characterId).enqueue(object : Callback<Personaje> {

            override fun onResponse(call: Call<Personaje>, response: Response<Personaje>) {

                //The body of the if is executed if the response from the API is correct, if it is not correct, the else is executed with the corresponding error code.
                if (response.isSuccessful) {

                    //character gets the response data
                    val character = response.body()

                    //if the response has content, the displayCharacter function is executed, otherwise the corresponding error is executed
                    if (character != null) {
                        displayCharacter(character)
                    }
                    else {
                        showError("No se encontró información del personaje con ese ID.")
                    }
                } else {

                    when (response.code()) {

                        404 -> showError("ID de personaje no encontrado.")
                        500 -> showError("Hubo un error del servidor.")
                        else -> showError("Error: ${response.code()}")
                    }
                }
                showLoading(false)

            }

               //executes if the API call could not be made due to a connection problem
            override fun onFailure(call: Call<Personaje>, t: Throwable) {

                showError("Error de conexión: ${t.message}")
                showLoading(false)
            }


           })

    }


    //function that searches for a character by name
    private fun searchCharacterByName(characterName: String) {

        showLoading(true)

        // call the API with Retrofit
        RetrofitClient.rickMortyApiService.getCharacter(characterName).enqueue(object : Callback<ApiResponse> {

            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {

                // the body of the if is executed if the response from the api is correct, if it is not correct the else is executed with the corresponding error code.
                if (response.isSuccessful) {

                    //characters gets a list of characters contained in result
                    val characters = response.body()?.results

                    //verify that characters is not empty and exists
                     if (characters != null && characters.isNotEmpty()) {

                         //displays the first element of the list
                        displayCharacter(characters[0])
                    }
                    else {
                        showError("No se encontró información del personaje.")
                    }
                }

                else {

                    when (response.code()) {
                        404 -> showError("Personaje no encontrado.")
                        500 -> showError("Hubo un error del servidor.")
                        else -> showError("Error: ${response.code()}")
                    }
                }
                showLoading(false)
            }



            //executes if the API call could not be made due to a connection problem
              override fun onFailure(call: Call<ApiResponse>, t: Throwable) {

                  showError("Error de conexión: ${t.message}")
                    showLoading(false)
            }

        })
    }


    //displays the data on the screen
    private fun displayCharacter(character: Personaje) {

        //multiple operations are applied to the same object

        binding.apply {

            // the card with the character's information is displayed
            cardCharacter.visibility = View.VISIBLE
            tvError.visibility = View.GONE



            //the field of each text view is filled for each character data
            tvCharacterName.text = "Nombre: ${character.name}"
            tvCharacterId.text = "ID: #${character.id}"
            tvCharacterStatus.text = "Estado: ${character.status}"
            tvCharacterSpecies.text = "Especie: ${character.species}"


            // the data that will be assigned if the show details button is pressed
            tvCharacterGender.text = "Género: ${character.gender}"
            tvCharacterOrigin.text = "Origen: ${character.origin.name}"
            tvCharacterLocation.text = "Ubicación: ${character.location.name}"



            //library that displays the character image
            Picasso.get()
                .load(character.image)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_background)
                .into(ivCharacter)




            // button functionality to show details
            btnExpandDetails.setOnClickListener {
                //If the details are hidden, it displays them and then changes the button text.
                //The body of the else does the opposite.
                if (extraDetails.visibility == View.GONE) {
                    extraDetails.visibility = View.VISIBLE
                    btnExpandDetails.text = "Ocultar detalles"
                } else {
                    extraDetails.visibility = View.GONE
                    btnExpandDetails.text = "Mostrar detalles"

                }

            }

        }
    }



    //a message is displayed in case of error
    private fun showError(message: String) {
         binding.tvError.text = message
        binding.tvError.visibility = VISIBLE
        binding.cardCharacter.visibility = GONE

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }



    //shows the progressBar to indicate to the user that the information is being loaded
    private fun showLoading(show: Boolean) {

        binding.progressBar.visibility = if (show) VISIBLE else GONE
        binding.btnSearch.isEnabled = !show
        binding.cardCharacter.visibility = if (show) GONE else binding.cardCharacter.visibility
        binding.tvError.visibility = if (show) GONE else binding.tvError.visibility
    }
}
