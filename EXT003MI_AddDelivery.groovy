// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to add a delivery to EXTDLH
// Transaction AddDelivery
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLTP - Delivery Type
 * @param: STAT - Status
 * @param: DLDT - Delivery Date
 * @param: SUNO - Supplier Number
 * @param: BUYE - Buyer
 * @param: DLFY - Deliver To Yard
 * @param: DLTY - Deliver To Yard
 * @param: FDCK - From Deck
 * @param: TDCK - To Deck
 * @param: RCPN - Receipt Number
 * @param: TRPN - Trip Ticket Number
 * @param: TRCK - Truck Number 
 * @param: BRND - Brand 
 * @param: DLST - Delivery Sub Type
 * @param: CTNO - Contract Number
 * @param: ALHA - Alternate Hauler
 * @param: ISZP - Zero Payment
 * @param: FACI - Facility
 * @param: RVID - Revision Id
 * @param: ISPS - Payee Split
 * @param: VLDT - Validate Date
 * @param: NOTE - Notes
 * 
*/


public class AddDelivery extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  // Definition 
  Integer CONO
  String DIVI
  int outDLNO 
  int inDLNO
  String nextNumber

  
  // Constructor 
  public AddDelivery(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
     this.mi = mi
     this.database = database
     this.miCaller = miCaller
     this.program = program
     this.utility = utility
     this.logger = logger
  } 
    
  public void main() {       
     // Set Company Number
     CONO = mi.in.get("CONO")      
     if (CONO == null || CONO == 0) {
        CONO = program.LDAZD.CONO as Integer
     } 

     // Set Division
     DIVI = mi.in.get("DIVI")
     if (DIVI == null || DIVI == "") {
        DIVI = program.LDAZD.DIVI
     }

     // Delivery Type
     int inDLTP
     if (mi.in.get("DLTP") != null) {
        inDLTP = mi.in.get("DLTP") 
     } else {
        inDLTP = 0         
     }
           
     // Status
     int inSTAT  
     if (mi.in.get("STAT") != null) {
        inSTAT = mi.in.get("STAT") 
     } else {
        inSTAT = 0        
     }
     
     // Delivery Date
     int inDLDT 
     if (mi.in.get("DLDT") != null) {
        inDLDT = mi.in.get("DLDT") 
     } else {
        inDLDT = 0        
     }

     // Supplier
     String inSUNO  
     if (mi.in.get("SUNO") != null) {
        inSUNO = mi.in.get("SUNO") 
     } else {
        inSUNO = ""        
     }

     // Buyer
     String inBUYE  
     if (mi.in.get("BUYE") != null) {
        inBUYE = mi.in.get("BUYE") 
     } else {
        inBUYE = ""      
     }

     // Deliver From Yard
     String inDLFY  
     if (mi.in.get("DLFY") != null) {
        inDLFY = mi.in.get("DLFY") 
     } else {
        inDLFY = ""        
     }

     // Deliver To Yard
     String inDLTY  
     if (mi.in.get("DLTY") != null) {
        inDLTY = mi.in.get("DLTY") 
     } else {
        inDLTY = ""        
     }

     // From Deck
     int inFDCK  
     if (mi.in.get("FDCK") != null) {
        inFDCK = mi.in.get("FDCK") 
     } else {
        inFDCK = 0        
     }

     // To Deck
     int inTDCK  
     if (mi.in.get("TDCK") != null) {
        inTDCK = mi.in.get("TDCK") 
     } else {
        inTDCK = 0        
     }

     // Receipt Number
     String inRCPN 
     if (mi.in.get("RCPN") != null) {
        inRCPN = mi.in.get("RCPN") 
     } else {
        inRCPN = ""        
     }

     // Trip Ticket Number
     String inTRPN
     if (mi.in.get("TRPN") != null) {
        inTRPN = mi.in.get("TRPN") 
     } else {
        inTRPN = ""        
     }
     
     // Truck Number
     String inTRCK
     if (mi.in.get("TRCK") != null) {
        inTRCK = mi.in.get("TRCK") 
     } else {
        inTRCK = ""        
     }

     // Brand
     String inBRND
     if (mi.in.get("BRND") != null) {
        inBRND = mi.in.get("BRND") 
     } else {
        inBRND = ""        
     }

     // Delivery Sub Type
     int inDLST
     if (mi.in.get("DLST") != null) {
        inDLST = mi.in.get("DLST") 
     } else {
        inDLST = 0        
     }
     
     // Contract Number
     int inCTNO 
     if (mi.in.get("CTNO") != null) {
        inCTNO = mi.in.get("CTNO") 
     } else {
        inCTNO = 0        
     }
     
     // Notes
     String inNOTE  
     if (mi.in.get("NOTE") != null) {
        inNOTE = mi.in.get("NOTE") 
     } else {
        inNOTE = ""        
     }

     // Alternate Hauler
     int inALHA
     if (mi.in.get("ALHA") != null) {
        inALHA = mi.in.get("ALHA") 
     } else {
        inALHA = 0        
     }

     // Zero Payment
     int inISZP
     if (mi.in.get("ISZP") != null) {
        inISZP = mi.in.get("ISZP") 
     } else {
        inISZP = 0        
     }

     // Facility
     String inFACI  
     if (mi.in.get("FACI") != null) {
        inFACI = mi.in.get("FACI") 
     } else {
        inFACI = ""        
     }
     
     // Revision Id
     String inRVID  
     if (mi.in.get("RVID") != null) {
        inRVID = mi.in.get("RVID") 
     } else {
        inRVID = ""        
     }
     
     // Payee Split
     int inISPS
     if (mi.in.get("ISPS") != null) {
        inISPS = mi.in.get("ISPS") 
     } else {
        inISPS = 0        
     }
     
     // Validate Date
     String inVLDT  
     if (mi.in.get("VLDT") != null) {
        inVLDT = mi.in.get("VLDT") 
     } else {
        inVLDT = ""        
     }

     if (inFACI != "" && inFACI != null) {
     } else {
        inFACI = getWarehouseMI(inDLTY)
     }

     //Get next number for contract number
     getNextNumber("", "L2", "1") 
     outDLNO = nextNumber as Integer
     inDLNO = outDLNO as Integer
     
     // Validate Delivery Head record
     Optional<DBContainer> EXTDLH = findEXTDLH(CONO, DIVI, inDLNO)
     if(EXTDLH.isPresent()){
        mi.error("Delivery already exists")   
        return             
     } else {
        // Write record 
        addEXTDLHRecord(CONO, DIVI, inDLTP, inSTAT, inDLDT, inSUNO, inBUYE, inDLFY, inDLTY, inFDCK, inTDCK, inRCPN, inTRPN, inTRCK, inBRND, inDLST, inCTNO, inNOTE, inALHA, inISZP, inFACI,inRVID,inISPS,inVLDT)          
     }  
     
     mi.outData.put("CONO", String.valueOf(CONO)) 
     mi.outData.put("DIVI", DIVI) 
     mi.outData.put("DLNO", String.valueOf(inDLNO))      
     mi.write()
  }
  

   //***************************************************************************** 
   // Get next number in the number serie using CRS165MI.RtvNextNumber    
   // Input 
   // Division
   // Number Series Type
   // Number Seriew
   //***************************************************************************** 
   def void getNextNumber(String division, String numberSeriesType, String numberSeries){   
        def params = [DIVI: division, NBTY: numberSeriesType, NBID: numberSeries] 
        def callback = {
        Map<String, String> response ->
          if(response.NBNR != null){
            nextNumber = response.NBNR
          }
        }

        miCaller.call("CRS165MI","RtvNextNumber", params, callback)
   } 
    
  //******************************************************************** 
  // Get EXTDLH record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDLH(int CONO, String DIVI, int DLNO){  
     DBAction query = database.table("EXTDLH").index("00").build()
     def EXTDLH = query.getContainer()
     EXTDLH.set("EXCONO", CONO)
     EXTDLH.set("EXDIVI", "")
     EXTDLH.set("EXDLNO", DLNO)
     if(query.read(EXTDLH))  { 
       return Optional.of(EXTDLH)
     } 
  
     return Optional.empty()
  }
  
  
   //***************************************************************************** 
   // Get Facility from Warehouse
   //***************************************************************************** 
   String getWarehouseMI(String warehouse) {   
        def params = [WHLO: warehouse] 
        String facility = null
        def callback = {
          Map<String, String> response ->
          if(response.FACI != null){
            facility = response.FACI 
          }
        }

        miCaller.call("MMS005MI","GetWarehouse", params, callback)        
        return facility
   } 

  
  //******************************************************************** 
  // Add EXTDLH record 
  //********************************************************************     
  void addEXTDLHRecord(int CONO, String DIVI, int DLTP, int STAT, int DLDT, String SUNO, String BUYE, String DLFY, String DLTY, int FDCK, int TDCK, String RCPN, String TRPN, String TRCK, String BRND, int DLST, int CTNO, String NOTE, int ALHA, int ISZP, String FACI, String RVID, int ISPS,String VLDT){  
       DBAction action = database.table("EXTDLH").index("00").build()
       DBContainer EXTDLH = action.createContainer()
       EXTDLH.set("EXCONO", CONO)
       EXTDLH.set("EXDIVI", DIVI)
       EXTDLH.set("EXDLNO", inDLNO)
       EXTDLH.set("EXDLTP", DLTP)
       EXTDLH.set("EXSTAT", STAT)
       EXTDLH.set("EXDLDT", DLDT)
       EXTDLH.set("EXSUNO", SUNO)
       EXTDLH.set("EXBUYE", BUYE)
       EXTDLH.set("EXDLFY", DLFY)
       EXTDLH.set("EXDLTY", DLTY)
       EXTDLH.set("EXFDCK", FDCK)
       EXTDLH.set("EXTDCK", TDCK)
       EXTDLH.set("EXRCPN", RCPN)
       EXTDLH.set("EXTRPN", TRPN)
       EXTDLH.set("EXTRCK", TRCK)
       EXTDLH.set("EXBRND", BRND)
       EXTDLH.set("EXDLST", DLST)
       EXTDLH.set("EXCTNO", CTNO)
       EXTDLH.set("EXNOTE", NOTE)
       EXTDLH.set("EXALHA", ALHA)
       EXTDLH.set("EXISZP", ISZP)
       EXTDLH.set("EXFACI", FACI)
       EXTDLH.set("EXRVID", RVID)
       EXTDLH.set("EXISPS", ISPS)
       EXTDLH.set("EXVLDT", VLDT)   
       EXTDLH.set("EXCHID", program.getUser())
       EXTDLH.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")    
       EXTDLH.set("EXRGDT", regdate) 
       EXTDLH.set("EXLMDT", regdate) 
       EXTDLH.set("EXRGTM", regtime)
       action.insert(EXTDLH)         
 } 

     
} 

