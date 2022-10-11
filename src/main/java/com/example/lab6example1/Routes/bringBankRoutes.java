package com.example.lab6example1.Routes;

import com.example.lab6example1.Entity.accountList;
import com.example.lab6example1.Entity.bringBank;
import com.example.lab6example1.Entity.updateclass;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.component.jackson.ListJacksonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class bringBankRoutes extends RouteBuilder {
    JacksonDataFormat format = new ListJacksonDataFormat(bringBank.class);
    @Override
    public void configure() throws Exception {
        restConfiguration().component("servlet");
        rest().path("/account")
                .description("per rest service")
                .consumes("application/json")
                .produces("application/json")
                .post()
                    .to("direct:addaccount")
                .get()
                .produces("application/json")
                .to("direct:viewallaccount")
                .get("/{id}")
                .produces("application/json")
                .to("direct:getcertainaccount")
                .put()
                .consumes("application/json")
                .to("direct:updateamount")
                .delete("/{id}")
                .consumes("application/json")
                .to("direct:deleteaccount")
                .put("/deposit")
                .consumes("application/json")
                .to("direct:depositamount")
                .put("/withdraw")
                .consumes("application/json")
                .to("direct:withdraw");

        from("direct:addaccount")
                .setBody(simple("${body}"))
                .unmarshal(new JacksonDataFormat(bringBank.class))
                .to("sql:INSERT INTO bringBank (firstName, lastName, accountNumber, amount) VALUES (:#${body.firstName},:#${body.lastName},:#${body.accountNumber},:#${body.amount} )")
                .transform().constant("Successfully Added The Account");

        from("direct:viewallaccount")
                .to("sql:{{sql.selectall}}")
                .setProperty("Bodyinobjectform",simple("${body}"))
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        accountList  bring = exchange.getProperty("Bodyinobjectform", accountList.class);
                         //int id = bring.getaccountList().get(0).getId();
                        System.out.println(bring.getaccountList());
                    }
                })
                .log("${body.id}")
                .marshal(new JacksonDataFormat(accountList.class))
                .setBody(simple("${body}"));

        from("direct:getcertainaccount")
                .to("sql:select * from bringBank where accountNumber=:#id")
                .marshal(new JacksonDataFormat(accountList.class))
                .setBody(simple("${body}"));

        from("direct:updateamount")
                .unmarshal(new JacksonDataFormat(updateclass.class))
                .to("sql:update bringBank set amount=:#${body.amount} where accountNumber=:#${body.accountNumber}")
                .setBody(simple("${body}"))
                .transform().constant("Successfully Updated an Account");

        from("direct:deleteaccount")
                .to("sql:delete from bringBank where accountNumber=:#id")
                .transform().constant("Successfully Deleted an Account");


        from("direct:depositamount")
                .unmarshal(new JacksonDataFormat(updateclass.class))
                .setHeader("accountNumber", simple("${body.accountNumber}"))
                .setHeader("amount", simple("${body.amount}"))
//                .log("${headers.accountNumber}")
                .to("sql:select amount from bringBank where accountNumber=:#accountNumber")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        List<Map<String, Object>> bring = (List<Map<String, Object>>) exchange.getIn().getBody();
                        Double amount = 0.00;
                        for (Map<String, Object> value : bring) {
//                            System.out.println(value.get("amount"));
                            amount = (double) value.get("amount");
                            exchange.getIn().setHeader("newamount", amount);
                        }

//                        System.out.println("This is the object from select >>> " + bring);
//                        System.out.println("This is the object from select amount >>> " + amount);
                    }
                })
//                .log("${headers.newamount}")
                .bean(updateclass.class,"depositamount(${headers.newamount}, ${headers.amount})")
                .setHeader("depositAmount", simple("${body}"))
                .log("${headers.amount}")
                .log("${headers.newamount}")
                .log("${headers.depositAmount}")
                .to("sql:update bringBank set amount=:#depositAmount where accountNumber=:#accountNumber")
                .setBody(simple("Successfully Deposited ${headers.amount} to your Account. Your new balance is ${headers.depositAmount}"));

        from("direct:withdraw")
                .unmarshal(new JacksonDataFormat(updateclass.class))
                .setHeader("accountNumber", simple("${body.accountNumber}"))
                .setHeader("amount", simple("${body.amount}"))
                .to("sql:select amount from bringBank where accountNumber=:#accountNumber")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        List<Map<String, Object>> bring = (List<Map<String, Object>>) exchange.getIn().getBody();
                        Double amount = 0.00;
                        for (Map<String, Object> value : bring) {
                            amount = (double) value.get("amount");
                            exchange.getIn().setHeader("newamount", amount);
                        }
                    }
                })
                .bean(updateclass.class,"withdrawamount(${headers.newamount}, ${headers.amount})")
                .setHeader("withdrawamount", simple("${body}"))
                .log("${headers.amount}")
                .log("${headers.newamount}")
                .log("${headers.withdrawamount}")
                .to("sql:update bringBank set amount=:#withdrawamount where accountNumber=:#accountNumber")
                .setBody(simple("Successfully Withdrawn ${headers.amount} to your Account. Your new balance is ${headers.withdrawamount}"));

    }
}
