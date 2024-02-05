// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to add a weight ticket line to EXTDWT
// Transaction AddWeightTktLn
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: WTNO - Weight Ticket ID
 * @param: DLNO - Delivery Number
 * @param: GRWE - Gross Weight
 * @param: TRWE - Tare Weight
 * @param: NEWE - Net Weight
*/


public class AddWeightTktLn extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  // Definition 
  Integer inCONO
  String inDIVI

  
  // Constructor 
  public AddWeightTktLn(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
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

     // Weight Ticket ID
     int inWTNO
     if (mi.in.get("WTNO") != null) {
        inWTNO = mi.in.get("WTNO") 
     } else {
        inWTNO = 0         
     }
     
     // Weight Ticket Number
     String inWTKN
     if (mi.in.get("WTKN") != null) {
        inWTKN = mi.in.get("WTKN") 
     } else {
        inWTKN = ""         
     }

     // Weight Date
     int inWTDT 
     if (mi.in.get("WTDT") != null) {
        inWTDT = mi.in.get("WTDT") 
     } else {
        inWTDT = 0        
     }
   
     // Weight Location Number
     String inWTLN
     if (mi.in.get("WTLN") != null) {
        inWTLN= mi.in.get("WTLN") 
     } else {
        inWTLN = ""         
     }
        
     // Delivery Number
     int inDLNO 
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0        
     }
     
     // Gross Weight
     double inGRWE  
     if (mi.in.get("GRWE") != null) {
        inGRWE = mi.in.get("GRWE") 
     } else {
        inGRWE = 0d        
     }

     // Tare Weight
     double inTRWE
     if (mi.in.get("TRWE") != null) {
        inTRWE = mi.in.get("TRWE") 
     } else {
        inTRWE = 0d        
     }

     // Net Weight
     double inNEWE
     if (mi.in.get("NEWE") != null) {
        inNEWE = mi.in.get("NEWE") 
     } else {
        inNEWE = 0d       
     }

     // Validate Weight Ticket Line record
     Optional<DBContainer> EXTDWT = findEXTDWT(inCONO, inDIVI, inWTNO, inDLNO)
     if(EXTDWT.isPresent()){
        mi.error("Weight Ticket Line already exist")   
        return             
     } else {
        // Write record 
        addEXTDWTRecord(inCONO, inDIVI, inWTNO, inWTKN, inWTDT, inWTLN, inDLNO, inGRWE, inTRWE, inNEWE)          
     }  
     
  }
  

  //******************************************************************** 
  // Get EXTDWT record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDWT(int CONO, String DIVI, int WTNO, int DLNO){  
     DBAction query = database.table("EXTDWT").index("00").build()
     DBContainer EXTDWT = query.getContainer()
     EXTDWT.set("EXCONO", CONO)
     EXTDWT.set("EXDIVI", DIVI)
     EXTDWT.set("EXWTNO", WTNO)
     EXTDWT.set("EXDLNO", DLNO)
     if(query.read(EXTDWT))  { 
       return Optional.of(EXTDWT)
     } 
  
     return Optional.empty()
  }
  
  //******************************************************************** 
  // Add EXTDWT record 
  //********************************************************************     
  void addEXTDWTRecord(int CONO, String DIVI, int WTNO, String WTKN, int WTDT, String WTLN, int DLNO, double GRWE, double TRWE, double NEWE){  
       DBAction action = database.table("EXTDWT").index("00").build()
       DBContainer EXTDWT = action.createContainer()
       EXTDWT.set("EXCONO", CONO)
       EXTDWT.set("EXDIVI", DIVI)
       EXTDWT.set("EXWTNO", WTNO)
       EXTDWT.set("EXWTKN", WTKN)
       EXTDWT.set("EXWTDT", WTDT)
       EXTDWT.set("EXWTLN", WTLN)
       EXTDWT.set("EXDLNO", DLNO)
       EXTDWT.set("EXGRWE", GRWE)
       EXTDWT.set("EXTRWE", TRWE)
       EXTDWT.set("EXNEWE", NEWE)   
       EXTDWT.set("EXCHID", program.getUser())
       EXTDWT.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")                    
       EXTDWT.set("EXRGDT", regdate) 
       EXTDWT.set("EXLMDT", regdate) 
       EXTDWT.set("EXRGTM", regtime)
       action.insert(EXTDWT)         
 } 
     
} 

