/**
 *  Copyright 2017 FinTx
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fintx.accounting.service;

import static org.junit.Assert.*;

import org.fintx.accounting.entity.Account;
import org.fintx.accounting.entity.AccountOpeningEntry;
import org.fintx.accounting.entity.OperationEntry;
import org.fintx.accounting.entity.TransactionEntry;
import org.fintx.accounting.entity.Voucher;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author bluecreator(qiang.x.wang@gmail.com)
 *
 */
public class DetailLedgerServiceTest {
    @Autowired
    AccountNoService accountNoService = null;

    
    @Autowired
    DetailLedgerService detailLedgerService = null;

    @Test
    public void test() {
//        OffsetDateTime offsetDateTime = OffsetDateTime.now();
//        System.out.println(offsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
//        LocalDate localDate = offsetDateTime.toLocalDate();
//        System.out.println(localDate.format(DateTimeFormatter.ISO_DATE));
//        offsetDateTime = offsetDateTime.withOffsetSameInstant(ZoneOffset.UTC);
//        System.out.println(offsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
//        localDate = offsetDateTime.toLocalDate();
//        System.out.println(localDate.format(DateTimeFormatter.ISO_DATE));
//        offsetDateTime = offsetDateTime.withOffsetSameInstant(ZoneOffset.MAX);
//        System.out.println(offsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
//        localDate = offsetDateTime.toLocalDate();
//        System.out.println(localDate.format(DateTimeFormatter.ISO_DATE));
        
        //build and persist accountNo
        AccountNoSection.Builder accountNoSectionBuilder=AccountNoSection.builder();
        //...
        AccountNoSection acocuntNoSection=accountNoSectionBuilder.build();
        String accountNo1 = accountNoService.createAccountNo(acocuntNoSection);
        //
        accountNo1=accountNoService.getAccountNo(acocuntNoSection);
        
        //Open account
        AccountOpening.Builder accountOpeningBuilder=AccountOpening.builder();
        //..
        AccountOpening accountOpening=accountOpeningBuilder.build();
        detailLedgerService.post(accountOpening);
        
        //audit accountOpening
        AccountOpeningEntry accountingOpeningEntry=detailLedgerService.auditAccountOpening(accountNo1);
        
        //audit opened account
        Account account1=detailLedgerService.auditAccount(accountNo1);
        
        //build voucher
        Voucher.Builder voucherBuilder=Voucher.builder();
        //...
        Voucher voucher = voucherBuilder.build();
        
        //post transaction for associated voucher
        String accountsCode = "11223344";
        String accountNo2 = "1122334455667777";
        String accountNo3 = "1122334455667788";
        Transaction.Builder transactionBuilder = Transaction.builder();
        transactionBuilder.associate(voucher);
        transactionBuilder.credit(accountsCode,accountNo1, new BigDecimal("100.00"));
        transactionBuilder.debit(accountsCode,accountNo2, new BigDecimal("50.00"));
        transactionBuilder.debit(accountsCode,accountNo3, new BigDecimal("50.00"));
        Transaction transaction = transactionBuilder.build();
        detailLedgerService.post(transaction);
        
        //audit transaction
        List<TransactionEntry> transactionEntries=detailLedgerService.auditTransaction(accountNo1, LocalDate.now(), voucher.getBusinessId());
        
        //operate account
        Operation.Builder operationBuilder=Operation.builder();
        operationBuilder.freeze(accountNo1, new BigDecimal("50.00"));
        operationBuilder.lock(accountNo1, voucher.getBusinessId());
        //...
        Operation operation=operationBuilder.build();
        detailLedgerService.post(operation);
        
        //audit operation
        List<OperationEntry> operationEntries= detailLedgerService.auditOperation(accountNo1, LocalDate.now(), voucher.getBusinessId());
      
    }

}
