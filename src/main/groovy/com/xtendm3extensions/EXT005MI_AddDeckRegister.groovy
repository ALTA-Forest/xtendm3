// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to add deck register to EXTDPR
// Transaction AddDeckRegister
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
 * @param: AUWE - Automatic Volume
 * @param: GRWE - Gross Weight
 * @param: TRWE - Tare Weight
 * @param: NEWE - Net Weight
 * @param: DAAM - Amount
 * @param: RPID - Reason ID
*/



public class AddDeckRegister extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  Integer inCONO
  String inDIVI
  int outTRNO
  int inTRNO
  int inTRDT 
  double detailLOAD
  double detailDLOG
  double detailGVBF
  double detailNVBF
  double detailCOST
  String nextNumber
  double headMBFW

  
  // Constructor 
  public AddDeckRegister(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
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
     inDIVI = mi.inData.get("DIVI").trim()
     if (inDIVI == null || inDIVI == "") {
        inDIVI = program.LDAZD.DIVI
     }

     // Deck ID
     int inDPID
     if (mi.in.get("DPID") != null) {
        inDPID = mi.in.get("DPID") 
     } else {
        inDPID = 0         
     }

     // Species
     String inSPEC 
     if (mi.inData.get("SPEC") != null) {
        inSPEC = mi.inData.get("SPEC").trim() 
     } else {
        inSPEC = ""        
     }

     // Purchase Order Number
     String inPUNO 
     if (mi.inData.get("PUNO") != null) {
        inPUNO = mi.inData.get("PUNO").trim() 
     } else {
        inPUNO = ""        
     }
     
     // Invoice Batch Number
     int inINBN 
     if (mi.in.get("INBN") != null) {
        inINBN = mi.in.get("INBN") 
     } else {
        inINBN = 0        
     }     

     // Transaction Date
     if (mi.in.get("TRDT") != null) {
        inTRDT = mi.in.get("TRDT") 
     } else {
        inTRDT = 0        
     }

     // Transaction Type
     int inTRTP 
     if (mi.in.get("TRTP") != null) {
        inTRTP = mi.in.get("TRTP") 
     } else {
        inTRTP = 0        
     }

     // Account Code
     String inACCD 
     if (mi.inData.get("ACCD") != null) {
        inACCD = mi.inData.get("ACCD").trim() 
     } else {
        inACCD = ""        
     }

     // Account Name
     String inACNM
     if (mi.inData.get("ACNM") != null) {
        inACNM = mi.inData.get("ACNM").trim() 
     } else {
        inACNM = ""        
     }
     
     // Transaction Receipt
     String inTRRE 
     if (mi.in.get("TRRE") != null) {
        inTRRE = mi.inData.get("TRRE").trim() 
     } else {
        inTRRE = ""        
     }
     
     // Transaction Ticket
     String inTRTT 
     if (mi.in.get("TRTT") != null) {
        inTRTT = mi.inData.get("TRTT").trim() 
     } else {
        inTRTT = ""        
     }
     
     // Load
     double inLOAD
     if (mi.in.get("LOAD") != null) {
        inLOAD = mi.in.get("LOAD") 
     }

     // Logs
     double inTLOG
     if (mi.in.get("TLOG") != null) {
        inTLOG = mi.in.get("TLOG") 
     } 

     // Gross BF
     double inTGBF
     if (mi.in.get("TGBF") != null) {
        inTGBF = mi.in.get("TGBF") 
     } 
     
     // Net BF
     double inTNBF 
     if (mi.in.get("TNBF") != null) {
        inTNBF = mi.in.get("TNBF") 
     } 

     // Net Balance
     double inTRNB 
     if (mi.in.get("TRNB") != null) {
        inTRNB = mi.in.get("TRNB") 
     } 

     // Automatic Volume
     double inAUWE 
     if (mi.in.get("AUWE") != null) {
        inAUWE = mi.in.get("AUWE") 
     }

     // Gross Weight
     double inGRWE
     if (mi.in.get("GRWE") != null) {
        inGRWE = mi.in.get("GRWE") 
     }

     // Tare Weight
     double inTRWE
     if (mi.in.get("TRWE") != null) {
        inTRWE = mi.in.get("TRWE") 
     } 

     // Net Weight
     double inNEWE
     if (mi.in.get("NEWE") != null) {
        inNEWE = mi.in.get("NEWE") 
     }

     // Amount
     double inDAAM
     if (mi.in.get("DAAM") != null) {
        inDAAM = mi.in.get("DAAM") 
     } 

     // Reason ID
     String inRPID
     if (mi.inData.get("RPID") != null) {
        inRPID = mi.inData.get("RPID").trim() 
     } else {
        inRPID = ""        
     }
 
     // Note
     String inNOTE
     if (mi.inData.get("NOTE") != null) {
        inNOTE = mi.inData.get("NOTE").trim() 
     } else {
        inNOTE = ""        
     }
    

     headMBFW = 0d
     
     // Validate Deck Head record
     Optional<DBContainer> EXTDPH = findEXTDPH(inCONO, inDIVI, inDPID)
     if(!EXTDPH.isPresent()){
        mi.error("Deck Head record doesn't exist")   
        return             
     } else {
        // Record found, get info from detail record  
        DBContainer containerEXTDPH = EXTDPH.get() 
        headMBFW = containerEXTDPH.get("EXMBFW") 
     }
     
     detailLOAD = 0
     detailDLOG = 0
     detailGVBF = 0d
     detailNVBF = 0d 
     detailCOST = 0d

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
        detailCOST = containerEXTDPD.get("EXCOST") 
     } 

     //Get next number for contract number
     getNextNumber("", "LP", "1") 
     outTRNO = nextNumber as Integer
     inTRNO = outTRNO as Integer
     
     // Validate Deck Register record
     Optional<DBContainer> EXTDPR = findEXTDPR(inCONO, inDIVI, inDPID, inTRNO)
     if(EXTDPR.isPresent()){
        mi.error("Deck Profile Details already exists")   
        return             
     } else {
        // Write record 
        addEXTDPRRecord(inCONO, inDIVI, inDPID, inTRNO, inSPEC, inPUNO, inINBN, inTRDT, inTRTP, inACCD, inACNM, inTRRE, inTRTT, inLOAD, inTLOG, inTGBF, inTNBF, inTRNB, inAUWE, inGRWE, inTRWE, inNEWE, inDAAM, inRPID, inNOTE)  
        
        //Sum of fields
        double totalLOAD
        double totalDLOG
        double totalGVBF
        double totalNVBF
        double totalDAAM
    
        if (inTRTP == 1 || inTRTP == 4 || inTRTP == 5) {
          //Purchase Order
          totalLOAD = inLOAD + detailLOAD
          totalDLOG = inTLOG + detailDLOG
          totalGVBF = inTGBF + detailGVBF
          totalNVBF = inTNBF + detailNVBF
          totalDAAM = inDAAM + detailCOST
        } else if (inTRTP == 2) {
          //Transfer
          totalLOAD = inLOAD + detailLOAD
          totalDLOG = inTLOG + detailDLOG
          totalGVBF = inTGBF + detailGVBF
          totalNVBF = inTNBF + detailNVBF
          totalDAAM = inDAAM + detailCOST
          if (headMBFW != 0 && headMBFW != null) {
          } else {
             inAUWE = 1
          }
        } else if (inTRTP == 3) {
           //Adjustments
          totalLOAD = inLOAD + detailLOAD
          totalDLOG = inTLOG + detailDLOG
          totalGVBF = inTGBF + detailGVBF
          totalNVBF = inTNBF + detailNVBF
          totalDAAM = inDAAM + detailCOST
          if (headMBFW != 0 && headMBFW != null) {
          } else {
             inAUWE = 1
          }
        }
        
        String inCONOString = String.valueOf(inCONO)
        String inDPIDString = String.valueOf(inDPID)
        String inLOADString = String.valueOf(totalLOAD)
        String inTLOGString = String.valueOf(totalDLOG)
        String inTGBFString = String.valueOf(totalGVBF)
        String inTNBFString = String.valueOf(totalNVBF)
        String inTCOIString = String.valueOf(totalDAAM)
        String inESWTString
        String inAWBFString 
        String inGBFLString
        String inNBFLString
             
        updDeckDetailsMI(inCONOString, inDIVI, inDPIDString, inLOADString, inTLOGString, inTGBFString, inTNBFString, inESWTString, inTCOIString, inAWBFString, inGBFLString, inNBFLString)   
     }  

  }


   //***************************************************************************** 
   // Get next number in the number serie using CRS165MI.RtvNextNumber    
   // Input 
   // Division
   // Number Series Type
   // Number Seriew
   //***************************************************************************** 
   void getNextNumber(String division, String numberSeriesType, String numberSeries){   
        Map<String, String> params = [DIVI: division, NBTY: numberSeriesType, NBID: numberSeries] 
        Closure<?> callback = {
        Map<String, String> response ->
          if(response.NBNR != null){
            nextNumber = response.NBNR
          }
        }

        miCaller.call("CRS165MI","RtvNextNumber", params, callback)
   } 


     //******************************************************************** 
     // Get EXTDPD record
     //******************************************************************** 
     private Optional<DBContainer> findEXTDPD(int CONO, String DIVI, int DPID) {  
        DBAction query = database.table("EXTDPD").index("00").selection("EXLOAD", "EXDLOG", "EXGVBF", "EXNVBF", "EXESWT", "EXCOST", "EXAWBF", "EXGBFL", "EXNBFL").build()
        DBContainer EXTDPD = query.getContainer()
        EXTDPD.set("EXCONO", CONO)
        EXTDPD.set("EXDIVI", DIVI)
        EXTDPD.set("EXDPID", DPID)
        if(query.read(EXTDPD))  { 
          return Optional.of(EXTDPD)
        } 
  
        return Optional.empty()
     }


     //******************************************************************** 
     // Get EXTDPH record
     //******************************************************************** 
     private Optional<DBContainer> findEXTDPH(int CONO, String DIVI, int DPID){  
        DBAction query = database.table("EXTDPH").index("00").selection("EXMBFW").build()
        DBContainer EXTDPH = query.getContainer()
        EXTDPH.set("EXCONO", CONO)
        EXTDPH.set("EXDIVI", DIVI)
        EXTDPH.set("EXDPID", DPID)
        if(query.read(EXTDPH))  { 
          return Optional.of(EXTDPH)
        } 
  
        return Optional.empty()
     }
  

   //***************************************************************************** 
   // Update Deck Details
   //***************************************************************************** 
   void updDeckDetailsMI(String company, String division, String deckID, String load, String log, String grossVolume, String netVolume, String estimatedWeight, String cost, String averageWeight, String averageGross, String averageNet) {   
        Map<String, String> params = [CONO: company, DIVI: division, DPID: deckID, LOAD: load, DLOG: log, GVBF: grossVolume, NVBF: netVolume, ESWT: estimatedWeight, TCOI: cost, AWBF: averageWeight, GBFL: averageGross, NBFL: averageNet] 
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
  // Add EXTDPR record 
  //********************************************************************     
  void addEXTDPRRecord(int CONO, String DIVI, int DPID, int TRNO, String SPEC, String PUNO, int INBN, int TRDT, int TRTP, String ACCD, String ACNM, String TRRE, String TRTT, double LOAD, double TLOG, double TGBF, double TNBF, double TRNB, double AUWE, double GRWE, double TRWE, double NEWE, double DAAM, String RPID, String NOTE){     
       DBAction action = database.table("EXTDPR").index("00").build()
       DBContainer EXTDPR = action.createContainer()
       EXTDPR.set("EXCONO", CONO)
       EXTDPR.set("EXDIVI", DIVI)
       EXTDPR.set("EXDPID", DPID)
       EXTDPR.set("EXTRNO", TRNO)
       EXTDPR.set("EXSPEC", SPEC)
       EXTDPR.set("EXPUNO", PUNO)
       EXTDPR.set("EXINBN", INBN)
       EXTDPR.set("EXTRDT", TRDT)
       EXTDPR.set("EXTRTP", TRTP)
       EXTDPR.set("EXACCD", ACCD)
       EXTDPR.set("EXACNM", ACNM)
       EXTDPR.set("EXTRRE", TRRE)
       EXTDPR.set("EXTRTT", TRTT)
       EXTDPR.set("EXLOAD", LOAD)
       EXTDPR.set("EXTLOG", TLOG)
       EXTDPR.set("EXTGBF", TGBF)
       EXTDPR.set("EXTNBF", TNBF)
       EXTDPR.set("EXTRNB", TRNB)
       EXTDPR.set("EXAUWE", AUWE)
       EXTDPR.set("EXGRWE", GRWE)
       EXTDPR.set("EXTRWE", TRWE)
       EXTDPR.set("EXNEWE", NEWE)
       EXTDPR.set("EXDAAM", DAAM)
       EXTDPR.set("EXRPID", RPID)
       EXTDPR.set("EXNOTE", NOTE)
       EXTDPR.set("EXCHID", program.getUser())
       EXTDPR.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")    
       EXTDPR.set("EXRGDT", regdate) 
       EXTDPR.set("EXLMDT", regdate) 
       EXTDPR.set("EXRGTM", regtime)
       action.insert(EXTDPR)         
 } 

     
} 

