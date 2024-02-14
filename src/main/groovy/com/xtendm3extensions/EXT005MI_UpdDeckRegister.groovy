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
  String inPUNO
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
     inDIVI = mi.inData.get("DIVI").trim()
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
     if (mi.in.get("SPEC") != null) {
        inSPEC = mi.inData.get("SPEC").trim() 
     } 

     // Purchase Order Number
     if (mi.inData.get("PUNO") != null) {
        inPUNO = mi.inData.get("PUNO").trim() 
     }
     
     // Invoice Batch Number
     if (mi.in.get("INBN") != null) {
        inINBN = mi.in.get("INBN") 
     }   

     // Transaction Date
     if (mi.in.get("TRDT") != null) {
        inTRDT = mi.in.get("TRDT") 
     }

     // Transaction Type
     if (mi.in.get("TRTP") != null) {
        inTRTP = mi.in.get("TRTP")
     } 

     // Account Code
     if (mi.in.get("ACCD") != null) {
        inACCD = mi.inData.get("ACCD").trim() 
     }
    
     // Account Name
     if (mi.inData.get("ACNM") != null) {
        inACNM = mi.inData.get("ACNM").trim() 
     }
 
     // Transaction Receipt
     if (mi.in.get("TRRE") != null) {
        inTRRE = mi.inData.get("TRRE").trim() 
     } 
     
     // Transaction Ticket
     if (mi.in.get("TRTT") != null) {
        inTRTT = mi.inData.get("TRTT").trim() 
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
     if (mi.inData.get("RPID") != null) {
        inRPID = mi.inData.get("RPID").trim() 
     }

     // Note
     if (mi.inData.get("NOTE") != null) {
        inNOTE = mi.inData.get("NOTE").trim() 
     } 


     // Validate Deck Profile Detail record
     Optional<DBContainer> EXTDPD = findEXTDPD(inCONO, inDIVI, inDPID)
     if(!EXTDPD.isPresent()){
        mi.error("Deck Profile Details doesn't exist")   
        return             
     } else {
        // Record found, get info from detail record  
        DBContainer containerEXTDPD = EXTDPD.get() 
        detailLOAD = containerEXTDPD.get("EXLOAD") 
        detailDLOG = containerEXTDPD.get("EXDLOG") 
        detailGVBF = containerEXTDPD.get("EXGVBF") 
        detailNVBF = containerEXTDPD.get("EXNVBF") 
     } 

     // Validate Deck Register record
     Optional<DBContainer> EXTDPR = findEXTDPR(inCONO, inDIVI, inDPID, inTRNO)
     if (!EXTDPR.isPresent()) {
        mi.error("Deck Register doesn't exist")   
        return             
     } else {       
        // Record found, get info from register record
        DBContainer containerEXTDPR = EXTDPR.get() 
        registerLOAD = containerEXTDPR.get("EXLOAD") 
        registerTLOG = containerEXTDPR.get("EXTLOG") 
        registerTGBF = containerEXTDPR.get("EXTGBF") 
        registerTNBF = containerEXTDPR.get("EXTNBF") 

        //Sum of fields
        double totalLOAD
        double totalDLOG
        double totalGVBF
        double totalNVBF
    
        totalLOAD = detailLOAD - registerLOAD
        totalDLOG = detailDLOG - registerTLOG
        totalGVBF = detailGVBF - registerTGBF
        totalNVBF = detailNVBF - registerTNBF
        
        totalLOAD = totalLOAD + inLOAD
        totalDLOG = totalLOAD + inTLOG
        totalGVBF = totalLOAD + inTGBF
        totalNVBF = totalLOAD + inTNBF


        String inCONOString = String.valueOf(inCONO)
        String inDPIDString = String.valueOf(inDPID)
        String inLOADString = String.valueOf(totalLOAD)
        String inTLOGString = String.valueOf(totalDLOG)
        String inTGBFString = String.valueOf(totalGVBF)
        String inTNBFString = String.valueOf(totalNVBF)
           
        updDeckDetailsMI(inCONOString, inDIVI, inDPIDString, inLOADString, inTLOGString, inTGBFString, inTNBFString)   

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
       if (inTRDT != null && inTRDT != "") {
          lockedResult.set("EXTRDT", inTRDT)
       }
  
       if (inSPEC != null && inSPEC != "") {
          lockedResult.set("EXSPEC", inSPEC)
       }
       
       if (inPUNO != null && inPUNO != "") {
          lockedResult.set("EXPUNO", inPUNO)
       }
  
       if (inINBN != null && inINBN != "") {
          lockedResult.set("EXINBN", inINBN)
       }
  
       if (inTRTP != null && inTRTP != "") {
          lockedResult.set("EXTRTP", inTRTP)
       }
   
       if (inACCD != null && inACCD != "") {
          lockedResult.set("EXACCD", inACCD)
       }
  
       if (inACNM != null && inACNM != "") {
          lockedResult.set("EXACNM", inACNM)
       }
       
       if (inTRRE != null && inTRRE != "") {
          lockedResult.set("EXTRRE", inTRRE)
       }
       
       if (inTRTT != null && inTRTT != "") {
          lockedResult.set("EXTRTT", inTRTT)
       }
       
       if (inLOAD != null && inLOAD != "") {
          lockedResult.set("EXLOAD", inLOAD)
       }
  
       if (inTLOG != null && inTLOG != "") {
          lockedResult.set("EXTLOG", inTLOG)
       }
       
       if (inTGBF != null && inTGBF != "") {
          lockedResult.set("EXTGBF", inTGBF)
       }
       
       if (inTNBF != null && inTNBF != "") {
          lockedResult.set("EXTNBF", inTNBF)
       }
       
       if (inTRNB != null && inTRNB != "") {
          lockedResult.set("EXTRNB", inTRNB)
       }
  
       if (inAUWE != null && inAUWE != "") {
          lockedResult.set("EXAUWE", inAUWE)
       }
  
       if (inGRWE != null && inGRWE != "") {
          lockedResult.set("EXGRWE", inGRWE)
       }
  
       if (inTRWE != null && inTRWE != "") {
          lockedResult.set("EXTRWE", inTRWE)
       }
  
       if (inNEWE != null && inNEWE != "") {
          lockedResult.set("EXNEWE", inNEWE)
       }
  
       if (inDAAM != null && inDAAM != "") {
          lockedResult.set("EXDAAM", inDAAM)
       }
  
       if (inRPID != null && inRPID != "") {
          lockedResult.set("EXRPID", inRPID)
       }
      
       if (inNOTE != null && inNOTE != "") {
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

} 

