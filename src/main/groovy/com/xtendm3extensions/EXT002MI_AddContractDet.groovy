// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API will add a new contract revision 
// Transaction AddContractDet
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: CTNO - Contract Number
 * @param: VALF - Valid From
 * @param: VALT - Valid To
 * 
*/
/**
 * OUT
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: RVID - Revision ID
 * @param: CTNO - Contract Number
 * @param: RVNO - Revision Number
 * 
*/


public class AddContractDet extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  // Definition 
  Integer inCONO
  String inDIVI
  int inCTNO
  String inRVID
  int inRVNO
  int outCTNO 
  int outRVNO
  int inVALF
  int inVALT
  int inSTAT
  String inPTPC
  String inRTPC
  String inTEPY
  int inFRSC
  String inCMNO
  String outRVID
  String nextNumber
  boolean dateNotValid
  int firstRevisionDate
  int lastRevisionDate

  
  // Constructor 
  public AddContractDet(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
     this.mi = mi
     this.database = database
     this.miCaller = miCaller
     this.program = program
     this.logger = logger
     this.utility = utility
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

     // Contract Number
     if (mi.in.get("CTNO") != null) {
        inCTNO = mi.in.get("CTNO") 
     } else {
        inCTNO = 0        
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
     
     inRVID = ""
     inRVNO = 0
     
     // Validate contract header
     Optional<DBContainer> EXTCTH = findEXTCTH(inCONO, inDIVI, inCTNO)
     if(!EXTCTH.isPresent()){
        mi.error("Contract Number doesn't exist in Contract Header table")   
        return             
     } else {   
        // Record found, get header information from contract  
        DBContainer containerEXTCTH = EXTCTH.get() 
        inRVID = containerEXTCTH.get("EXRVID") 
        inRVNO = containerEXTCTH.get("EXRVNO") 
     } 
     
     // Get contract detail record from latest revision
     Optional<DBContainer> EXTCTD = findEXTCTD(inCONO, inDIVI, inRVID)
     if(EXTCTD.isPresent()){
       // Record found, get detail information from contract  
       DBContainer containerEXTCTD = EXTCTD.get() 
       outCTNO = containerEXTCTD.get("EXCTNO") 
       Integer revisionNumber = containerEXTCTD.get("EXRVNO") 
       Integer newRevisionNumber = revisionNumber + 1
       outRVNO = newRevisionNumber
       outRVID = String.valueOf(outCTNO) + String.valueOf(newRevisionNumber)
       
       inSTAT = containerEXTCTD.get("EXSTAT") 
       inPTPC = containerEXTCTD.get("EXPTPC") 
       inRTPC = containerEXTCTD.get("EXRTPC") 
       inTEPY = containerEXTCTD.get("EXTEPY") 
       inFRSC = containerEXTCTD.get("EXFRSC") 
       inCMNO = containerEXTCTD.get("EXCMNO") 
       
     } else {
       mi.error("Contract Number/Revision ID doesn't exist in Contract Details table")   
       return             
     }
     
     dateNotValid = false       
     validateInputDates()       
     if (dateNotValid) { 
         mi.error("Date Range is not valid")           
         return             
     } else {
         // Write record 
         addEXTCTDRecord(inCONO, inDIVI, inCTNO, outRVNO, outRVID, inVALF, inVALT, inPTPC, inRTPC, inTEPY, inFRSC, inCMNO) 
         getContractHeaderDates()
         updEXTCTHRecord()   
     }

     mi.outData.put("CONO", String.valueOf(inCONO)) 
     mi.outData.put("DIVI", inDIVI) 
     mi.outData.put("CTNO", String.valueOf(inCTNO)) 
     mi.outData.put("RVID", outRVID) 
     mi.outData.put("RVNO", String.valueOf(outRVNO)) 
     mi.write()
  }
  
  
  //******************************************************************** 
  // Get EXTCTH record
  //******************************************************************** 
  private Optional<DBContainer> findEXTCTH(int CONO, String DIVI, int CTNO){  
     DBAction query = database.table("EXTCTH").index("00").selection("EXRVNO", "EXRVID").build()
     DBContainer EXTCTH = query.getContainer()
     EXTCTH.set("EXCONO", CONO)
     EXTCTH.set("EXDIVI", DIVI)
     EXTCTH.set("EXCTNO", CTNO)
     if(query.read(EXTCTH))  { 
       return Optional.of(EXTCTH)
     } 
  
     return Optional.empty()
  }
  
  //******************************************************************** 
  // Get EXTCTD record
  //******************************************************************** 
  private Optional<DBContainer> findEXTCTD(int CONO, String DIVI, String RVID){  
     DBAction query = database.table("EXTCTD").index("00").selection("EXCTNO", "EXRVNO", "EXSTAT", "EXPTPC", "EXRTPC", "EXTEPY", "EXFRSC", "EXCMNO").build()
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
   // Validate the date is not overlapping
   //********************************************************************  
   void validateInputDates(){   
     
     dateNotValid = false
     
     //Read all revisions for the contract to check dates
     DBAction queryEXTCTD = database.table("EXTCTD").index("10").selection("EXVALF", "EXVALT").build()    
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

      if (inVALF > 0 && inVALT > 0) {
        
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
     DBAction queryEXTCTDdates = database.table("EXTCTD").index("60").selection("EXCONO", "EXDIVI", "EXCTNO", "EXRVID", "EXRVNO", "EXVALF", "EXVALT").build()    
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
  // Add EXTCTD record 
  //********************************************************************     
  void addEXTCTDRecord(int CONO, String DIVI, int CTNO, int RVNO, String RVID, int VALF, int VALT, String PTPC, String RTPC, String TEPY, int FRSC, String CMNO){     
       DBAction action = database.table("EXTCTD").index("00").build()
       DBContainer EXTCTD = action.createContainer()
       EXTCTD.set("EXCONO", CONO)
       EXTCTD.set("EXDIVI", DIVI)
       EXTCTD.set("EXCTNO", CTNO)
       EXTCTD.set("EXRVNO", RVNO)
       EXTCTD.set("EXRVID", RVID)
       EXTCTD.set("EXVALF", VALF)
       EXTCTD.set("EXVALT", VALT)
       EXTCTD.set("EXSTAT", 10)
       EXTCTD.set("EXCHID", program.getUser())
       EXTCTD.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")    
       EXTCTD.set("EXRGDT", regdate) 
       EXTCTD.set("EXLMDT", regdate) 
       EXTCTD.set("EXRGTM", regtime)
       action.insert(EXTCTD)         
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
       lockedResult.set("EXRVNO", outRVNO)
       lockedResult.set("EXRVID", outRVID)
       int changeNo = lockedResult.get("EXCHNO")
       int newChangeNo = changeNo + 1 
       int changeddate = utility.call("DateUtil", "currentDateY8AsInt")
       lockedResult.set("EXLMDT", changeddate)        
       lockedResult.set("EXCHNO", newChangeNo) 
       lockedResult.set("EXCHID", program.getUser())
       lockedResult.update()
    }
     
} 

