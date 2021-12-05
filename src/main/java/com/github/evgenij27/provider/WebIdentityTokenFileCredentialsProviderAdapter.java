package com.github.evgenij27.provider;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.WebIdentityTokenFileCredentialsProvider;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebIdentityTokenFileCredentialsProviderAdapter implements AWSCredentialsProvider {

    private final AwsCredentialsProvider provider;

    private final ScheduledExecutorService executor;

    private volatile AWSCredentials credentials;

    public WebIdentityTokenFileCredentialsProviderAdapter() {
        this.provider = WebIdentityTokenFileCredentialsProvider.create();
        this.executor = Executors.newScheduledThreadPool(1);
    }

    private AWSCredentials resolveCredentials() {
        AwsSessionCredentials awsCredentials = (AwsSessionCredentials) provider.resolveCredentials();
        return new BasicSessionCredentials(awsCredentials.secretAccessKey(),
            awsCredentials.secretAccessKey(), awsCredentials.sessionToken());
    }

    @Override
    public AWSCredentials getCredentials() {
        if (credentials == null) {
            credentials = resolveCredentials();
            executor.schedule(() -> {
                credentials = null;
            }, 55, TimeUnit.MINUTES);
        }
        return credentials;
    }

    @Override
    public void refresh() {
        credentials = resolveCredentials();
    }
}
