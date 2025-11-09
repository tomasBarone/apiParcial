package com.example.apiparcial

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
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

    // Stores the history of the last 5 successful searches.
    private val searchHistory = mutableListOf<String>()
    private val MAX_HISTORY_SIZE = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListener()
    }


    /**
     * Configures the click listeners for the Search and History buttons.
     */
    private fun setupClickListener() {

        // Listener for the Search button
        binding.btnSearch.setOnClickListener {

            // characterInput gets what the user types, trimming spaces
            val characterInput = binding.tieCharacterInput.text.toString().trim()


            // If there is something written in the input, the if body is executed
            if (characterInput.isNotEmpty()) {

                // Tries to search by ID first
                val characterId = characterInput.toIntOrNull()
                if (characterId != null) {
                    searchCharacterById(characterInput)
                }
                // If it is not an ID, search by name
                else {
                    searchCharacterByName(characterInput)
                }
            }
            else {
                Toast.makeText(this, "Por favor, ingresa un ID o nombre", Toast.LENGTH_LONG).show()
            }
        }

        // Final Listener for the History button
        binding.btnHistory.setOnClickListener {
            showHistoryDialog()
        }
    }


    /**
     * Function that searches for a character by its exact ID.
     */
    private fun searchCharacterById(characterId: String) {

        showLoading(true)

        // Calls the API with Retrofit to get a character by ID
        RetrofitClient.rickMortyApiService.getCharacterById(characterId).enqueue(object : Callback<Personaje> {

            override fun onResponse(call: Call<Personaje>, response: Response<Personaje>) {

                // Executes if the API response is correct (2xx code)
                if (response.isSuccessful) {

                    // character gets the response data
                    val character = response.body()

                    // if the response has content, the displayCharacter function is executed
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

            // Executes if the API call could not be made due to connection problems
            override fun onFailure(call: Call<Personaje>, t: Throwable) {

                showError("Error de conexión: ${t.message}")
                showLoading(false)
            }


        })

    }


    /**
     * Function that searches for characters by name (may return multiple results).
     */
    private fun searchCharacterByName(characterName: String) {

        showLoading(true)

        // Calls the API with Retrofit (searches the general search endpoint)
        RetrofitClient.rickMortyApiService.getCharacter(characterName).enqueue(object : Callback<ApiResponse> {

            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {

                // Executes if the API response is correct.
                if (response.isSuccessful) {

                    // characters gets a list of characters contained in result
                    val characters = response.body()?.results

                    // verifies that characters is not empty and exists
                    if (characters != null && characters.isNotEmpty()) {

                        // Multiple results logic
                        if (characters.size > 1) {
                            val message = "Se encontraron ${characters.size} coincidencias. Mostrando el primer resultado."
                            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                        }

                        // Shows the first item in the list (the most relevant result)
                        displayCharacter(characters[0])
                    }
                    else {
                        showError("No se encontró información del personaje con ese nombre.")
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


            // Executes if the API call could not be made due to connection problems
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {

                showError("Error de conexión: ${t.message}")
                showLoading(false)
            }

        })
    }


    /**
     * Displays the character data in the CardView.
     */
    private fun displayCharacter(character: Personaje) {

        // Updates the history when displaying the character
        updateHistory(character.name)

        // Multiple operations are applied to the same object
        binding.apply {

            // The card with the character information is displayed
            cardCharacter.visibility = VISIBLE
            tvError.visibility = GONE

            // Each text view field is filled for each character data point
            tvCharacterName.text = "Nombre: ${character.name}"
            tvCharacterId.text = "ID: #${character.id}"
            tvCharacterStatus.text = "Estado: ${character.status}"
            tvCharacterSpecies.text = "Especie: ${character.species}"


            // Data that will be assigned if the show details button is pressed
            tvCharacterGender.text = "Género: ${character.gender}"
            tvCharacterOrigin.text = "Origen: ${character.origin.name}"
            tvCharacterLocation.text = "Ubicación: ${character.location.name}"


            // Library that displays the character image (Picasso)
            Picasso.get()
                .load(character.image)
                .placeholder(R.drawable.ic_launcher_foreground) // Example Placeholder
                .error(R.drawable.ic_launcher_background)      // Example error image
                .into(ivCharacter)


            // Functionality of the button to show/hide details
            btnExpandDetails.setOnClickListener {
                if (extraDetails.visibility == GONE) {
                    extraDetails.visibility = VISIBLE
                    btnExpandDetails.text = "Ocultar detalles"
                } else {
                    extraDetails.visibility = GONE
                    btnExpandDetails.text = "Mostrar detalles"

                }

            }

        }
    }


    // === SEARCH HISTORY FUNCTIONS ===

    /**
     * Adds the character name to the history, maintaining a maximum of [MAX_HISTORY_SIZE] items.
     */
    private fun updateHistory(characterName: String) {
        // Remove if it already exists to move it to the beginning (most recent)
        searchHistory.remove(characterName)

        // Add the new name to the start of the list
        searchHistory.add(0, characterName)

        // Limit the size to MAX_HISTORY_SIZE
        if (searchHistory.size > MAX_HISTORY_SIZE) {
            searchHistory.removeAt(searchHistory.size - 1)
        }
        Log.d("History", "Historial actualizado: $searchHistory")
    }

    /**
     * Shows the recent history in an interactive Dialog, allowing a previous search to be selected.
     */
    private fun showHistoryDialog() {
        if (searchHistory.isEmpty()) {
            Toast.makeText(this, "El historial de búsqueda está vacío.", Toast.LENGTH_SHORT).show()
            return
        }

        // Convert the list to an Array for the AlertDialog
        val historyArray = searchHistory.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Historial de Búsquedas Recientes")
            .setItems(historyArray) { dialog, which ->
                // 'which' is the index of the clicked item
                val selectedName = historyArray[which]
                Toast.makeText(this, "Buscando: $selectedName", Toast.LENGTH_SHORT).show()
                // Perform the search again with the selected name
                binding.tieCharacterInput.setText(selectedName) // Fills the text field with the name
                searchCharacterByName(selectedName)
            }
            .setNegativeButton("Cerrar", null) // Button to close the dialog
            .show()
    }

    // ===============================================

    /**
     * Shows an error message and hides the CardView.
     */
    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = VISIBLE
        binding.cardCharacter.visibility = GONE

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }


    /**
     * Shows the ProgressBar and disables the buttons during loading.
     */
    private fun showLoading(show: Boolean) {

        binding.progressBar.visibility = if (show) VISIBLE else GONE
        binding.btnSearch.isEnabled = !show
        // Ensures the history button is also disabled
        binding.btnHistory.isEnabled = !show

        // Hides the card and error if loading
        if (show) {
            binding.cardCharacter.visibility = GONE
            binding.tvError.visibility = GONE
        }
    }
}