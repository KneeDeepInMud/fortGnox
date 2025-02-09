package org.mockenhaupt.fortgnox.otp;

public class OTP
{
    String name = "";
    String issuer = "";
    String algorithm = "";
    int digits;
    int period = 30;
    String secret = "";

    public OTP()
    {
    }


    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getIssuer()
    {
        return issuer;
    }

    public String getAlgorithm()
    {
        return algorithm;
    }

    public int getDigits()
    {
        return digits;
    }

    public int getPeriod()
    {
        return period;
    }

    public String getSecret()
    {
        return secret;
    }


    public void setIssuer(String issuer)
    {
        this.issuer = issuer;
    }

    public void setAlgorithm(String algorithm)
    {
        this.algorithm = algorithm;
    }

    public void setDigits(int digits)
    {
        this.digits = digits;
    }

    public void setDigits(String digits)
    {
        this.digits = Integer.parseInt(digits);
    }

    public void setPeriod(int period)
    {
        this.period = period;
    }

    public void setPeriod(String period)
    {
        this.period = Integer.parseInt(period);
    }

    public void setSecret(String secret)
    {
        this.secret = secret;
    }


}
