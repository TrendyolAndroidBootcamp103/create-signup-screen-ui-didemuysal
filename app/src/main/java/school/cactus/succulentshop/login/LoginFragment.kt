package school.cactus.succulentshop.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar
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
import school.cactus.succulentshop.api.login.LoginRequest
import school.cactus.succulentshop.api.login.LoginResponse
import school.cactus.succulentshop.auth.JwtStore
import school.cactus.succulentshop.databinding.FragmentLoginBinding
import school.cactus.succulentshop.infra.BaseFragment
import school.cactus.succulentshop.login.validation.IdentifierValidator
import school.cactus.succulentshop.login.validation.PasswordValidator

class LoginFragment : BaseFragment() {
    private var _binding: FragmentLoginBinding? = null

    private val binding get() = _binding!!

    private val identifierValidator = IdentifierValidator()
    private val passwordValidator = PasswordValidator()

    override val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(
            store = JwtStore(requireContext()),
            repository = LoginRepository()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            logInButton.setOnClickListener {
                if (passwordInputLayout.isValid() and identifierInputLayout.isValid()) {
                    sendLoginRequest()
                }
            }
            createAccountButton.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
            }
        }

        requireActivity().title = getString(R.string.log_in)
    }
    private fun sendLoginRequest() {
        val identifier = binding.identifierInputLayout.editText!!.text.toString()
        val password = binding.passwordInputLayout.editText!!.text.toString()

        val request = LoginRequest(identifier, password)

        api.login(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                when (response.code()) {
                    200 -> onLoginSuccess(response.body()!!)
                    in 400..499 -> onClientError(response.errorBody())
                    else -> onUnexpectedError()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Snackbar.make(
                    binding.root, R.string.check_your_connection,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.retry) {
                        sendLoginRequest()
                    }
                    .show()
            }
        })
    }
    private fun onUnexpectedError() {
        Snackbar.make(
            binding.root, R.string.unexpected_error_occurred,
            BaseTransientBottomBar.LENGTH_LONG
        ).show()
    }

    private fun onClientError(errorBody: ResponseBody?) {
        if (errorBody == null) return onUnexpectedError()

        try {
            val message = errorBody.errorMessage()
            Snackbar.make(binding.root, message, BaseTransientBottomBar.LENGTH_LONG).show()
        } catch (ex: JsonSyntaxException) {
            onUnexpectedError()
        }
    }

    private fun ResponseBody.errorMessage(): String {
        val errorBody = string()
        val gson: Gson = GsonBuilder().create()
        val loginErrorResponse = gson.fromJson(errorBody, GenericErrorResponse::class.java)
        return loginErrorResponse.message[0].messages[0].message
    }

    private fun onLoginSuccess(response: LoginResponse) {
        JwtStore(requireContext()).saveJwt(response.jwt)
        findNavController().navigate(R.id.loginSuccessful)
    }

    private fun TextInputLayout.isValid(): Boolean {
        val errorMessage = validator().validate(editText!!.text.toString())
        error = errorMessage?.resolveAsString()
        isErrorEnabled = errorMessage != null
        return errorMessage == null
    }

    private fun Int.resolveAsString() = getString(this)

    private fun TextInputLayout.validator() = when (this) {
        binding.identifierInputLayout -> identifierValidator
        binding.passwordInputLayout -> passwordValidator
        else -> throw IllegalArgumentException("Cannot find any validator for the given TextInputLayout")
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}