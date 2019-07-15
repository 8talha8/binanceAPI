package com.mstakx.binanceAPIMain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.mstakx.srvc.BinanceSrv;
//@ComponentScan
//@EnableAutoConfiguration
@SpringBootApplication(scanBasePackages={"com.mstakx.srvc", "com.mstakx"})

public class BinanceApiApplication implements CommandLineRunner{
	@Autowired
    private BinanceSrv binanceSrv;
	public static void main(String[] args) {
		SpringApplication.run(BinanceApiApplication.class, args);
	}
	
	@Override
    public void run(String... args) throws Exception {

        try {
			if (args.length > 0) {
//        	System.out.println(binanceSrv.getMessage());
			} else {
			    System.out.println(binanceSrv.do1());
//            
			}

			System.exit(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}
