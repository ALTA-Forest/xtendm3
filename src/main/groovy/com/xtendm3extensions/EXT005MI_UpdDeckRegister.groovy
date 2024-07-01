// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to update deck register in EXTDPR
// Transaction UpdDeckRegister
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DPID - Deck ID
 * @param: TRNO - Transaction Number
 * @param: TRDT - Transaction Date
 * @param: TRTP - Transaction Type
 * @param: ACCD - Account Code
 * @param: TRRE - Transaction Receipt
 * @param: TRTT - Transaction Ticket
 * @param: LOAD - Load
 * @param: TGBF - Gross BF
 * @param: TNBF - Net BF
 * @param: TRNB - Net Balance
 * 
*/



public class UpdDeckRegister extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inDPID
  int inTRNO 
  String inSPEC
  int inINBN 
  String inTREF
  int inTRDT 
  int inTRTP 
  String inACCD  
  String inACNM
  String inTRRE 
  String inTRTT 
  double inAUWE 
  double inGRWE
  double inTRWE
  double inNEWE       
  double inLOAD
  double inTLOG
  double inTGBF
  double inTNBF 
  double inTRNB 
  double inDAAM
  String inRPID
  String inNOTE
  double detailLOAD
  double detailDLOG
  double detailGVBF
  double detailNVBF
  double registerLOAD
  double registerTLOG
  double registerTGBF
  double registerTNBF
  double totalLOAD
  double totalDLOG
  double totalGVBF
  double totalNVBF
  double inDLOD
  double inDLOG
  double inDGBF
  double inCOST
  int transactionNumber
     
  
  // Constructor 
  public UpdDeckRegister(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
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

     // Deck ID
     if (mi.in.get("DPID") != null) {
        inDPID = mi.in.get("DPID") 
     } else {
        inDPID = 0         
     }

     // Transaction Number
     if (mi.in.get("TRNO") != null) {
        inTRNO = mi.in.get("TRNO") 
     } else {
        inTRNO = 0         
     }           

     // Species
     if (mi.in.get("SPEC") != null && mi.in.get("SPEC") != "") {
        inSPEC = mi.inData.get("SPEC").trim() 
     } else {
        inSPEC = ""
     }
     
     // Transaction Type
     if (mi.in.get("TRTP") != null) {
        inTRTP = mi.in.get("TRTP")
     }      

     // Reference Number
     if (mi.in.get("TREF") != null && mi.in.get("TREF") != "") {
        inTREF = mi.inData.get("TREF").trim() 
     } else {
        inTREF = ""
     }
     
     // Invoice Batch Number
     if (mi.in.get("INBN") != null) {
        inINBN = mi.in.get("INBN") 
     }   

     // Transaction Date
     if (mi.in.get("TRDT") != null) {
        inTRDT = mi.in.get("TRDT") 
        
        //Validate date format
        boolean validTRDT = utility.call("DateUtil", "isDateValid", String.valueOf(inTRDT), "yyyyMMdd")  
        if (!validTRDT) {
           mi.error("Transaction Date is not valid")   
           return  
        } 

     }

     // Account Code
     if (mi.in.get("ACCD") != null && mi.in.get("ACCD") != "") {
        inACCD = mi.inData.get("ACCD").trim() 
     } else {
        inACCD = ""
     }
    
     // Account Name
     if (mi.in.get("ACNM") != null && mi.in.get("ACNM") != "") {
        inACNM = mi.inData.get("ACNM").trim() 
     } else {
        inACNM = ""
     }
 
     // Transaction Receipt
     if (mi.in.get("TRRE") != null && mi.in.get("TRRE") != "") {
        inTRRE = mi.inData.get("TRRE").trim() 
     } else {
        inTRRE = ""
     }
     
     // Transaction Ticket
     if (mi.in.get("TRTT") != null && mi.in.get("TRTT") != "") {
        inTRTT = mi.inData.get("TRTT").trim() 
     } else {
        inTRTT = ""
     }
     
     // Load
     if (mi.in.get("LOAD") != null) {
        inLOAD = mi.in.get("LOAD") 
     } 

     // Logs
     if (mi.in.get("TLOG") != null) {
        inTLOG = mi.in.get("TLOG") 
     }

     // Gross BF
     if (mi.in.get("TGBF") != null) {
        inTGBF = mi.in.get("TGBF") 
     }
     
     // Net BF
     if (mi.in.get("TNBF") != null) {
        inTNBF = mi.in.get("TNBF") 
     }

     // Net Balance
     if (mi.in.get("TRNB") != null) {
        inTRNB = mi.in.get("TRNB") 
     } 

     // Automatic Volume
     if (mi.in.get("AUWE") != null) {
        inAUWE = mi.in.get("AUWE") 
     }

     // Gross Weight
     if (mi.in.get("GRWE") != null) {
        inGRWE = mi.in.get("GRWE") 
     }

     // Tare Weight
     if (mi.in.get("TRWE") != null) {
        inTRWE = mi.in.get("TRWE") 
     }

     // Net Weight
     if (mi.in.get("NEWE") != null) {
        inNEWE = mi.in.get("NEWE") 
     }

     // Amount
     if (mi.in.get("DAAM") != null) {
        inDAAM = mi.in.get("DAAM") 
     }

     // Reason ID
     if (mi.in.get("RPID") != null && mi.in.get("RPID") != "") {
        inRPID = mi.inData.get("RPID").trim() 
     } else {
        inRPID = ""
     }
     
      // Total Load
     if (mi.in.get("DLOD") != null) {
        inDLOD = mi.in.get("DLOD") 
     } 

     // Total Logs
     if (mi.in.get("DLOG") != null) {
        inDLOG = mi.in.get("DLOG") 
     }

     // Total Gross BF
     if (mi.in.get("DGBF") != null) {
        inDGBF = mi.in.get("DGBF") 
     }
     
     // Total Amount
     if (mi.in.get("COST") != null) {
        inCOST = mi.in.get("COST") 
     }

     // Note
     if (mi.in.get("NOTE") != null && mi.in.get("NOTE") != "") {
        inNOTE = mi.inData.get("NOTE").trim() 
     } else {
        inNOTE = ""
     }

     
    //Get last TRNB for the deck
     List<DBContainer> ResultEXTDPR = listEXTDPR(inCONO, inDIVI, inDPID, inTRNO)
     for (DBContainer RecLineEXTDPR : ResultEXTDPR){ 
        transactionNumber = RecLineEXTDPR.get("EXTRNO") 
     }

     // Validate Deck Register record
     Optional<DBContainer> EXTDPR = findEXTDPR(inCONO, inDIVI, inDPID, inTRNO)
     if (!EXTDPR.isPresent()) {
        mi.error("Deck Register doesn't exist")   
        return             
     } else {       
        if (inTRNO >= transactionNumber) {
          updEXTDPDRecord()
        }

        // Update record
        updEXTDPRRecord()
     }
     
  }
  

  //******************************************************************** 
  // Get EXTDPD record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDPD(int CONO, String DIVI, int DPID) {  
      DBAction query = database.table("EXTDPD").index("00").selection("EXLOAD", "EXDLOG", "EXGVBF", "EXNVBF").build()
      DBContainer EXTDPD = query.getContainer()
      EXTDPD.set("EXCONO", CONO)
      EXTDPD.set("EXDIVI", DIVI)
      EXTDPD.set("EXDPID", DPID)
      if(query.read(EXTDPD))  { 
        return Optional.of(EXTDPD)
      } 
  
      return Optional.empty()
   }


  //***************************************************************************** 
  // Update Deck Details
  //***************************************************************************** 
  void updDeckDetailsMI(String company, String division, String deckID, String load, String log, String grossVolume, String netVolume){   
      Map<String, String> params = [CONO: company, DIVI: division, DPID: deckID, LOAD: load, DLOG: log, GVBF: grossVolume, NVBF: netVolume] 
      Closure<?> callback = {
        Map<String, String> response ->
      }
        
      miCaller.call("EXT005MI","UpdDeckDet", params, callback)
   } 
  
  
      
  //******************************************************************** 
  // Get EXTDPR record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDPR(int CONO, String DIVI, int DPID, int TRNO){  
     DBAction query = database.table("EXTDPR").index("00").build()
     DBContainer EXTDPR = query.getContainer()
     EXTDPR.set("EXCONO", CONO)
     EXTDPR.set("EXDIVI", DIVI)
     EXTDPR.set("EXDPID", DPID)
     EXTDPR.set("EXTRNO", TRNO)
     if(query.read(EXTDPR))  { 
       return Optional.of(EXTDPR)
     } 
  
     return Optional.empty()
  }


  //******************************************************************** 
  // Update EXTDPR record
  //********************************************************************    
  void updEXTDPRRecord(){      
     DBAction action = database.table("EXTDPR").index("00").build()
     DBContainer EXTDPR = action.getContainer()     
     EXTDPR.set("EXCONO", inCONO)
     EXTDPR.set("EXDIVI", inDIVI)
     EXTDPR.set("EXDPID", inDPID)
     EXTDPR.set("EXTRNO", inTRNO)

     // Read with lock
     action.readLock(EXTDPR, updateCallBackEXTDPR)
     }
   
     Closure<?> updateCallBackEXTDPR = { LockedResult lockedResult -> 
       if (mi.in.get("TRDT") != null) {
          lockedResult.set("EXTRDT", inTRDT)
       }
  
       if (mi.in.get("SPEC") != null && mi.in.get("SPEC") != "") {
          lockedResult.set("EXSPEC", inSPEC)
       }
       
       if (mi.in.get("TREF") != null && mi.in.get("TREF") != "") {
          lockedResult.set("EXTREF", inTREF)
       }
  
       if (mi.in.get("INBN") != null) {
          lockedResult.set("EXINBN", inINBN)
       }
  
       if (mi.in.get("TRTP") != null) {
          lockedResult.set("EXTRTP", inTRTP)
       }
   
       if (mi.in.get("ACCD") != null && mi.in.get("ACCD") != "") {
          lockedResult.set("EXACCD", inACCD)
       }
  
       if (mi.in.get("ACNM") != null && mi.in.get("ACNM") != "") {
          lockedResult.set("EXACNM", inACNM)
       }
       
       if (mi.in.get("TRRE") != null && mi.in.get("TRRE") != "") {
          lockedResult.set("EXTRRE", inTRRE)
       }
       
       if (mi.in.get("TRTT") != null && mi.in.get("TRTT") != "") {
          lockedResult.set("EXTRTT", inTRTT)
       }
       
       if (mi.in.get("LOAD") != null) {
          lockedResult.set("EXLOAD", inLOAD)
       }
  
       if (mi.in.get("TLOG") != null) {
          lockedResult.set("EXTLOG", inTLOG)
       }
       
       if (mi.in.get("TGBF") != null) {
          lockedResult.set("EXTGBF", inTGBF)
       }
       
       if (mi.in.get("TNBF") != null) {
          lockedResult.set("EXTNBF", inTNBF)
       }
       
       if (mi.in.get("TRNB") != null) {
          lockedResult.set("EXTRNB", inTRNB)
       }
  
       if (mi.in.get("AUWE") != null) {
          lockedResult.set("EXAUWE", inAUWE)
       }
  
       if (mi.in.get("GRWE") != null) {
          lockedResult.set("EXGRWE", inGRWE)
       }
  
       if (mi.in.get("TRWE") != null) {
          lockedResult.set("EXTRWE", inTRWE)
       }
  
       if (mi.in.get("NEWE") != null) {
          lockedResult.set("EXNEWE", inNEWE)
       }
  
       if (mi.in.get("DAAM") != null) {
          lockedResult.set("EXDAAM", inDAAM)
       }
  
       if (mi.in.get("RPID") != null && mi.in.get("RPID") != "") {
          lockedResult.set("EXRPID", inRPID)
       }
      
       if (mi.in.get("NOTE") != null && mi.in.get("NOTE") != "") {
          lockedResult.set("EXNOTE", inNOTE)
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
    // Update EXTDPD record
    //********************************************************************    
    void updEXTDPDRecord() {      
       DBAction action = database.table("EXTDPD").index("00").build()
       DBContainer EXTDPD = action.getContainer()     
       EXTDPD.set("EXCONO", inCONO)
       EXTDPD.set("EXDIVI", inDIVI)
       EXTDPD.set("EXDPID", inDPID)

       // Read with lock
       action.readLock(EXTDPD, updateCallBackEXTDPD)
     }
   
     Closure<?> updateCallBackEXTDPD = { LockedResult lockedResult -> 
         if (mi.in.get("TRNB") != null) {
            lockedResult.set("EXNVBF", inTRNB)
         }
         // Total Load
         if (mi.in.get("DLOD") != null) {
            lockedResult.set("EXLOAD", inDLOD)
         } 
         // Total Logs
         if (mi.in.get("DLOG") != null) {
            lockedResult.set("EXDLOG", inDLOG)
         }
         // Total Gross BF
         if (mi.in.get("DGBF") != null) {
            lockedResult.set("EXGVBF", inDGBF)
         }
         // Total Amount
         if (mi.in.get("COST") != null) {
            lockedResult.set("EXCOST", inCOST)
         }
    

        // Update changed information
        int changeNo = lockedResult.get("EXCHNO")
        int newChangeNo = changeNo + 1 
        int changeddate = utility.call("DateUtil", "currentDateY8AsInt")
        lockedResult.set("EXLMDT", changeddate)        
        lockedResult.set("EXCHNO", newChangeNo) 
        lockedResult.set("EXCHID", program.getUser())
        lockedResult.update()
     }
     
     //******************************************************************** 
     // Get last TRNB for the deck
     //********************************************************************  
     private List<DBContainer> listEXTDPR(int CONO, String DIVI, int DPID, int TRNO){  
       List<DBContainer>recLineEXTDPR = new ArrayList() 
       ExpressionFactory expression = database.getExpressionFactory("EXTDPR")
       expression = expression.eq("EXCONO", String.valueOf(CONO)).and(expression.eq("EXDIVI", DIVI)).and(expression.eq("EXDPID", String.valueOf(DPID)))
      
       DBAction query = database.table("EXTDPR").index("00").matching(expression).selection("EXTRNO", "EXTRNB").reverse().build()
       DBContainer EXTDPR = query.createContainer()
       EXTDPR.set("EXCONO", CONO)
       EXTDPR.set("EXDIVI", DIVI)
       EXTDPR.set("EXDPID", DPID)
       EXTDPR.set("EXTRNO", TRNO)
  
       int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords() 
      
       boolean found = false
       query.readAll(EXTDPR, 3, pageSize, { DBContainer recordEXTDPR ->  
       if (!found) {
          recLineEXTDPR.add(recordEXTDPR.createCopy())
          found = true
       }
      })
  
      return recLineEXTDPR
    }


} 

