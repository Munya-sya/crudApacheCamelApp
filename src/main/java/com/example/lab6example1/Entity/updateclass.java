package com.example.lab6example1.Entity;

public class updateclass {
    private String accountNumber;
    private double amount;
    public updateclass(){

    }
    public updateclass(String accountNumber, double amount) {
        this.accountNumber = accountNumber;
        this.amount = amount;
    }
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double depositamount(double amountintheaccount, double amountdeposited){
        double newamount  = amountintheaccount + amountdeposited;
        return newamount;
    }
    public double withdrawamount(double amountintheaccount, double amountentered ){
        if(amountintheaccount>amountentered){
            return amountintheaccount-amountentered;
        }else{
            return amountentered;
        }
    }


}
