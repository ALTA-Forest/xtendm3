// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to update contract details in EXTCTD 
// Transaction UpdContractDet
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: RVID - Revision ID
 * @param: VALF - Valid From
 * @param: VALT - Valid To
 * @param: STAT - Status
 * @param: PTPC - Permit Type
 * @param: RTPC - Rate Type
 * @param: TEPY - Payment Terms
 * @param: FRSC - Frequency Scaling
 * @param: CMNO - Compliance Number
 * @param: PTDT - Permit Date
 * 
*/



public class UpdContractDet extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger

  Integer inCONO
  String inDIVI
  String inRVID
  String contractNumber
  int inCTNO
  int inRVNO
  int inVALF
  int inVALT
  int inSTAT
  String inPTPC
  String inRTPC
  String inTEPY
  int inFRSC
  String inCMNO
  boolean dateNotValid
  int firstRevisionDate
  int lastRevisionDate
  int inPTDT

  // Constructor 
  public UpdContractDet(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
     this.mi = mi
     this.database = database
     this.miCaller = miCaller
     this.program = program
     this.utility = utility
     this.logger = logger     
  } 
    
  public void main() {       
     // Set Company Number
     inCONO = mi.in.get("CONO")      
     if (inCONO == null || inCONO == 0) {
        inCONO = program.LDAZD.CONO as Integer
     } 

     // Set Division
     inDIVI = mi.in.get("DIVI")
     if (inDIVI == null || inDIVI == "") {
        inDIVI = program.LDAZD.DIVI
     }

     // Revision ID
     if (mi.in.get("RVID") != null) {
        inRVID = mi.in.get("RVID") 
     } 
     
     // Valid From
     if (mi.in.get("VALF") != null) {
        inVALF = mi.in.get("VALF") 
        
        //Validate date format
        boolean validVALF = utility.call("DateUtil", "isDateValid", String.valueOf(inVALF), "yyyyMMdd")  
        if (!validVALF) {
           mi.error("Valid From Date is not valid")   
           return  
        } 

     } else {
        inVALF = 0        
     }
         
     // Valid To
     if (mi.in.get("VALT") != null) {
        inVALT = mi.in.get("VALT") 
        
        //Validate date format
        boolean validVALT = utility.call("DateUtil", "isDateValid", String.valueOf(inVALT), "yyyyMMdd")  
        if (!validVALT) {
           mi.error("Valid To Date is not valid")   
           return  
        } 

     } else {
        inVALT = 0        
     }
     
     // Status
     if (mi.in.get("STAT") != null) {
        inSTAT = mi.in.get("STAT") 
     } else {
        inSTAT = 0        
     }
 
     // Permit Type
     if (mi.in.get("PTPC") != null && mi.in.get("PTPC") != "") {
        inPTPC = mi.inData.get("PTPC").trim() 
     } else {
        inPTPC = ""        
     }
    
     // Rate Type
     if (mi.in.get("RTPC") != null) {
        inRTPC = mi.in.get("RTPC") 
     } else {
        inRTPC = ""        
     }
  
     // Payment Terms
     if (mi.in.get("TEPY") != null && mi.in.get("TEPY") != "") {
        inTEPY = mi.inData.get("TEPY").trim() 
        
        // Validate Payment Terms
        Optional<DBContainer> CSYTAB = findCSYTAB(inCONO, inTEPY, "")
        if (!CSYTAB.isPresent()) {
           mi.error("Payment Terms doesn't exist")   
           return  
        } 

     } else {
        inTEPY = ""        
     }

     // Frequency Scaling
     if (mi.in.get("FRSC") != null) {
        inFRSC = mi.in.get("FRSC") 
     }

     // Compliance Number
     if (mi.in.get("CMNO") != null && mi.in.get("CMNO") != "") {
        inCMNO = mi.inData.get("CMNO").trim() 
     } else {
        inCMNO = ""        
     }
     
     // Expiration Date
     if (mi.in.get("PTDT") != null) {
        inPTDT = mi.in.get("PTDT") 
        
        //Validate date format
        boolean validPTDT = utility.call("DateUtil", "isDateValid", String.valueOf(inPTDT), "yyyyMMdd")  
        if (!validPTDT) {
           mi.error("Permit Date is not valid")   
           return  
        } 

     } 


     // Get contract detail record
     Optional<DBContainer> EXTCTD = findEXTCTD(inCONO, inDIVI, inRVID)
     if(!EXTCTD.isPresent()){
        mi.error("Contract Number/Revision ID doesn't exist in Contract Details table")   
        return             
     } else {
        DBContainer containerEXTCTD = EXTCTD.get() 
        inCTNO = containerEXTCTD.get("EXCTNO") 
        inRVNO = containerEXTCTD.get("EXRVNO") 
     }

     dateNotValid = false
       
     validateInputDates()

     if (dateNotValid) { 
         mi.error("Date Range is not valid")           
         return             
     } else {
        // Update record
        updEXTCTDRecord()
        getContractHeaderDates()
        updEXTCTHRecord()
     }
     
  }
  


   //******************************************************************** 
   // Validate the date is not overlapping
   //********************************************************************  
   void validateInputDates(){   
     
     dateNotValid = false

     //Read all revisions for the contract to check dates
     DBAction queryEXTCTD = database.table("EXTCTD").index("10").selection("EXRVID", "EXVALF", "EXVALT").build()    
     DBContainer containerEXTCTD = queryEXTCTD.getContainer()
     containerEXTCTD.set("EXCONO", inCONO)
     containerEXTCTD.set("EXDIVI", inDIVI)
     containerEXTCTD.set("EXCTNO", inCTNO)
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
     queryEXTCTD.readAll(containerEXTCTD, 3, pageSize, releasedLineProcessorEXTCTD)
   
   } 

    
  //******************************************************************** 
  // Check dates - main loop
  //********************************************************************  
  Closure<?> releasedLineProcessorEXTCTD = { DBContainer containerEXTCTD -> 

      // Output
      int fromDate = containerEXTCTD.get("EXVALF") 
      int toDate = containerEXTCTD.get("EXVALT") 
      String revision = containerEXTCTD.getString("EXRVID") 

      if (inVALF > 0 && inVALT > 0 && !revision.trim().equals(inRVID.trim())) {

        if (inVALF == fromDate && inVALT == toDate) {
           mi.error("Same Date Range already exist")   
           dateNotValid = true
           return                       
        }
        
        if (((inVALF >= fromDate) && (inVALF <= toDate)) || ((inVALT >= fromDate) && (inVALT <= toDate)) ) {
           mi.error("Date Range is not valid")   
           dateNotValid = true
           return             
        }
        
        if ((inVALF <= fromDate) && (inVALT >= toDate) ) {
          mi.error("Date Range is not valid")   
          dateNotValid = true
          return             
        }

      }

  }
    
   //******************************************************************** 
   // Get first and last date to update on the contract header
   //********************************************************************  
   void getContractHeaderDates(){   

     firstRevisionDate = 99999999
     lastRevisionDate = 0 
     
     //Read all revisions for the contract to check dates
     DBAction queryEXTCTDdates = database.table("EXTCTD").index("60").selection("EXVALF", "EXVALT").build()    
     DBContainer containerEXTCTDdates = queryEXTCTDdates.getContainer()
     containerEXTCTDdates.set("EXCONO", inCONO)
     containerEXTCTDdates.set("EXDIVI", inDIVI)
     containerEXTCTDdates.set("EXCTNO", inCTNO)
     containerEXTCTDdates.set("EXSTAT", 20)

     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
     queryEXTCTDdates.readAll(containerEXTCTDdates, 4, pageSize, releasedLineProcessorEXTCTDdates)
   
   } 

    
  //******************************************************************** 
  // Get dates - main loop
  //********************************************************************  
  Closure<?> releasedLineProcessorEXTCTDdates = { DBContainer containerEXTCTDdates -> 

      int fromDate = containerEXTCTDdates.get("EXVALF")
      int toDate = containerEXTCTDdates.get("EXVALT")

      if (fromDate < firstRevisionDate) {
        firstRevisionDate = containerEXTCTDdates.get("EXVALF") 
      }
      
      if (toDate > lastRevisionDate) {
        lastRevisionDate = containerEXTCTDdates.get("EXVALT") 
      }

  }


  //******************************************************************** 
  // Check TEPY in CSYTAB
  //******************************************************************** 
 private Optional<DBContainer> findCSYTAB(Integer CONO, String STKY, String LNCD){  
    DBAction query = database.table("CSYTAB").index("00").build()     
    DBContainer CSYTAB = query.getContainer()
    CSYTAB.set("CTCONO", CONO)
    CSYTAB.set("CTDIVI", "")
    CSYTAB.set("CTSTCO", "TEPY")
    CSYTAB.set("CTSTKY", STKY)
    CSYTAB.set("CTLNCD", LNCD)
    
    if(query.read(CSYTAB))  { 
      return Optional.of(CSYTAB)
    } 
  
    return Optional.empty()
  }
  
  
  //******************************************************************** 
  // Get EXTCTD record
  //******************************************************************** 
  private Optional<DBContainer> findEXTCTD(int CONO, String DIVI, String RVID){  
     DBAction query = database.table("EXTCTD").index("00").selection("EXCTNO", "EXRVNO").build()
     DBContainer EXTCTD = query.getContainer()
     EXTCTD.set("EXCONO", CONO)
     EXTCTD.set("EXDIVI", DIVI)
     EXTCTD.set("EXRVID", RVID)
     if(query.read(EXTCTD))  { 
       return Optional.of(EXTCTD)
     } 
  
     return Optional.empty()
  }


  //******************************************************************** 
  // Update EXTCTD record
  //********************************************************************    
  void updEXTCTDRecord(){     
     DBAction action = database.table("EXTCTD").index("00").build()
     DBContainer EXTCTD = action.getContainer()
     EXTCTD.set("EXCONO", inCONO)  
     EXTCTD.set("EXDIVI", inDIVI)   
     EXTCTD.set("EXRVID", inRVID)

     // Read with lock
     action.readLock(EXTCTD, updateCallBackEXTCTD)
     }
   
     Closure<?> updateCallBackEXTCTD = { LockedResult lockedResult -> 
       if (inVALF != 0) {
          lockedResult.set("EXVALF", inVALF)
       }
  
       if (inVALT != 0) {     
          lockedResult.set("EXVALT", inVALT)
       }
       
       if (inSTAT > 0) {
          lockedResult.set("EXSTAT", inSTAT)
       }
       
       if (inPTPC != "") {
          lockedResult.set("EXPTPC", inPTPC)
       }
       
       if (inRTPC != "") {
          lockedResult.set("EXRTPC", inRTPC)
       }
  
       if (inTEPY != "") {                    
          lockedResult.set("EXTEPY", inTEPY)
       }
       
       if (mi.in.get("FRSC") != null) {
          lockedResult.set("EXFRSC", mi.in.get("FRSC"))
       }
       
       if (inCMNO != "") {
          lockedResult.set("EXCMNO", inCMNO)
       }
       
       if (mi.in.get("PTDT") != null) {
          lockedResult.set("EXPTDT", mi.in.get("PTDT"))
       }

        
       int changeNo = lockedResult.get("EXCHNO")
       int newChangeNo = changeNo + 1 
       int changeddate = utility.call("DateUtil", "currentDateY8AsInt")
       lockedResult.set("EXLMDT", changeddate)       
       lockedResult.set("EXCHNO", newChangeNo) 
       lockedResult.set("EXCHID", program.getUser())
       lockedResult.update()
  }


  //******************************************************************** 
  // Update EXTCTH record
  //********************************************************************    
  void updEXTCTHRecord(){          
     DBAction action = database.table("EXTCTH").index("00").build()
     DBContainer EXTCTH = action.getContainer()
     EXTCTH.set("EXCONO", inCONO)  
     EXTCTH.set("EXDIVI", inDIVI)   
     EXTCTH.set("EXCTNO", inCTNO)

     // Read with lock
     action.readLock(EXTCTH, updateCallBackEXTCTH)
     }
   
     Closure<?> updateCallBackEXTCTH = { LockedResult lockedResult -> 
       lockedResult.set("EXVALF", firstRevisionDate)
       lockedResult.set("EXVALT", lastRevisionDate)

       int changeNo = lockedResult.get("EXCHNO")
       int newChangeNo = changeNo + 1 
       int changeddate = utility.call("DateUtil", "currentDateY8AsInt")
       lockedResult.set("EXLMDT", changeddate)       
       lockedResult.set("EXCHNO", newChangeNo) 
       lockedResult.set("EXCHID", program.getUser())
       lockedResult.update()
  }

} 

