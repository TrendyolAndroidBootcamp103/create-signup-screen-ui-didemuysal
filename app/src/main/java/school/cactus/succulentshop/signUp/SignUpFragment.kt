package school.cactus.succulentshop.signUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import school.cactus.succulentshop.R
import school.cactus.succulentshop.api.GenericErrorResponse
import school.cactus.succulentshop.api.api
import school.cactus.succulentshop.api.register.RegisterRequest
import school.cactus.succulentshop.api.register.RegisterResponse
import school.cactus.succulentshop.auth.JwtStore
import school.cactus.succulentshop.databinding.FragmentSignUpBinding
import school.cactus.succulentshop.signUp.validation.MailValidator
import school.cactus.succulentshop.signUp.validation.UsernameValidator

class SignUpFragment : Fragment() {
    private var _binding: FragmentSignUpBinding? = null

    private val binding get() = _binding!!

    private val mailValidator = MailValidator()
    private val usernameValidator = UsernameValidator()
    private val passwordValidator =
        school.cactus.succulentshop.signUp.validation.PasswordValidator()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            signUpButton.setOnClickListener {
                if (signUpUsername.isValid() and signUpEmail.isValid()
                    and signUpPassword.isValid()
                ) {
                    sendRegisterRequest()
                }
            }

            goToSingInButton.setOnClickListener {
                findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
            }
        }

        requireActivity().title = getString(R.string.sign_up)
    }

    private fun sendRegisterRequest() {
        val email = binding.signUpEmail.editText!!.text.toString()
        val password = binding.signUpPassword.editText!!.text.toString()
        val username = binding.signUpUsername.editText!!.text.toString()

        val request = RegisterRequest(email, password, username)

        api.register(request).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
                when (response.code()) {
                    200 -> onRegisterSuccess(response.body()!!)
                    in 400..499 -> onClientError(response.errorBody())
                    else -> onUnexpectedError()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Snackbar.make(
                    binding.root, R.string.check_your_connection,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.retry) {
                        sendRegisterRequest()
                    }
                    .show()
            }
        })
    }

    private fun onUnexpectedError() {
        Snackbar.make(binding.root, R.string.unexpected_error_occurred, LENGTH_LONG).show()
    }

    private fun onClientError(errorBody: ResponseBody?) {
        if (errorBody == null) return onUnexpectedError()

        try {
            val message = errorBody.errorMessage()
            Snackbar.make(binding.root, message, LENGTH_LONG).show()
        } catch (ex: JsonSyntaxException) {
            onUnexpectedError()
        }
    }

    private fun onRegisterSuccess(response: RegisterResponse) {
        JwtStore(requireContext()).saveJwt(response.jwt)
        findNavController().navigate(R.id.action_signUpFragment_to_productListFragment)
    }


    private fun ResponseBody.errorMessage(): String {
        val errorBody = string()
        val gson: Gson = GsonBuilder().create()
        val loginErrorResponse = gson.fromJson(errorBody, GenericErrorResponse::class.java)
        return loginErrorResponse.message[0].messages[0].message
    }

    private fun TextInputLayout.isValid(): Boolean {
        val errorMessage = validator().validate(editText!!.text.toString())
        errorMessage?.let {
            error = resolveAsString(it)
        }
        isErrorEnabled = errorMessage != null
        return errorMessage == null
    }

    private fun resolveAsString(number: Int) = getString(number)

    private fun TextInputLayout.validator() = when (this) {
        binding.signUpEmail -> mailValidator
        binding.signUpUsername -> usernameValidator
        binding.signUpPassword -> passwordValidator
        else -> throw IllegalArgumentException("Cannot find any validator for the given TextInputLayout")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
