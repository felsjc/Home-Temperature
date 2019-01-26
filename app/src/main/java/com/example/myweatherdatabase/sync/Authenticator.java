package com.example.myweatherdatabase.sync;


import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.content.Context;
import android.os.Bundle;

public class Authenticator extends AbstractAccountAuthenticator {

    Authenticator(final Context context) {
        super(context);
    }

    /**
     * Returns a Bundle that contains the Intent of the activity that can be used to edit the
     * properties. In order to indicate success the activity should call response.setResult() with a
     * non-null Bundle.
     *
     * @param response    used to set the result for the request. If the Constants.INTENT_KEY is set
     *                    in the bundle then this response field is to be used for sending future
     *                    results if and when the Intent is started.
     * @param accountType the AccountType whose properties are to be edited.
     * @return a Bundle containing the result or the Intent to start to continue the request. If
     * this is null then the request is considered to still be active and the result should sent
     * later using response.
     */
    @Override
    public Bundle editProperties(final AccountAuthenticatorResponse response,
                                 final String accountType) {
        throw new UnsupportedOperationException();
    }

    /**
     * Adds an account of the specified accountType.
     *
     * @param response         to send the result back to the AccountManager, will never be null
     * @param accountType      the type of account to add, will never be null
     * @param authTokenType    the type of auth token to retrieve after adding the account, may be
     *                         null
     * @param requiredFeatures a String array of authenticator-specific features that the added
     *                         account must support, may be null
     * @param options          a Bundle of authenticator-specific options, may be null
     * @return a Bundle result or null if the result is to be returned via the response.
     */
    @Override
    public Bundle addAccount(final AccountAuthenticatorResponse response, final String accountType,
                             final String authTokenType, final String[] requiredFeatures,
                             final Bundle options) {
        return null;
    }

    /**
     * Checks that the user knows the credentials of an account.
     *
     * @param response to send the result back to the AccountManager, will never be null
     * @param account  the account whose credentials are to be checked, will never be null
     * @param options  a Bundle of authenticator-specific options, may be null
     * @return a Bundle result or null if the result is to be returned via the response.
     */
    @Override
    public Bundle confirmCredentials(final AccountAuthenticatorResponse response,
                                     final Account account,
                                     final Bundle options) {
        return null;
    }


    @Override
    public Bundle getAuthToken(final AccountAuthenticatorResponse response, final Account account,
                               final String authTokenType, final Bundle options) {
        throw new UnsupportedOperationException();
    }

    /**
     * Ask the authenticator for a localized label for the given authTokenType.
     *
     * @param authTokenType the authTokenType whose label is to be returned, will never be null
     * @return the localized label of the auth token type, may be null if the type isn't known
     */
    @Override
    public String getAuthTokenLabel(final String authTokenType) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Bundle updateCredentials(final AccountAuthenticatorResponse response,
                                    final Account account,
                                    final String authTokenType, final Bundle options) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Bundle hasFeatures(final AccountAuthenticatorResponse response, final Account account,
                              final String[] features) {
        throw new UnsupportedOperationException();
    }
}
