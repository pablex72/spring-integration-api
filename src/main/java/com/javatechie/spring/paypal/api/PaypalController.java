package com.javatechie.spring.paypal.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/paypal")
public class PaypalController {

	@Autowired
	PaypalService service;

	@Autowired
	HttpServletRequest request;

	public static final String SUCCESS_URL = "paypal/pay/success";
	public static final String CANCEL_URL = "paypal/pay/cancel";



	@GetMapping("/")
	public String home() {
		return "home";
	}

	@PostMapping("/pay")
	public String payment(@ModelAttribute("order") Order order) {
		try {
			Payment payment = service.createPayment(order.getPrice(), order.getCurrency(), order.getMethod(),
					order.getIntent(), order.getDescription(), "http://localhost:9090/" + CANCEL_URL,
					"http://localhost:9090/" + SUCCESS_URL);
			//return to paypal payment procesing page
			for(Links link:payment.getLinks()) {
				if(link.getRel().equals("approval_url")) {
					return "redirect:"+link.getHref();
				}
			}
		} catch (PayPalRESTException e) {
		
			e.printStackTrace();
		}
		return "redirect:/";
	}
	
	@GetMapping(value = CANCEL_URL)
		public ResponseEntity<?> cancelPay() {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Process Canceled");
		//return "cancel";
		}

	//As you can see, this code captures the values of PayerID and paymentId sent from PayPal
	@GetMapping(value = SUCCESS_URL)
	public ResponseEntity<?> successPay(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId) {
		try {
			Payment payment = service.executePayment(paymentId, payerId);
			System.out.println(payment.toJSON());
			if (payment.getState().equals("approved")) {
				return ResponseEntity.status(HttpStatus.OK).body("Successful Process");
			}
		} catch (PayPalRESTException e) {
		 System.out.println(e.getMessage());
		}
		return ResponseEntity.status(HttpStatus.OK).body("Redirect Successful Process");
	}
}
