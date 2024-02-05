// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to update a contract instruction in EXTDWT
// Transaction UpdWeightTktLn
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: WTNO - Weight Ticket ID
 * @param: WTKN - Weight Ticket Number
 * @param: WTDT - Weight Date
 * @param: WTLN - Weight Location Number
 * @param: DLNO - Delivery Number
 * @param: GRWE - Gross Weight
 * @param: TRWE - Tare Weight
 * @param: NEWE - Net Weight
*/



public class UpdWeightTktLn extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inWTNO
  String inWTKN
  int inWTDT
  String inWTLN
  int inDLNO
  double inGRWE
  double inTRWE
  double inNEWE
  
  // Constructor 
  public UpdWeightTktLn(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
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

     // Weight Ticket ID
     if (mi.in.get("WTNO") != null) {
        inWTNO = mi.in.get("WTNO") 
     } else {
        inWTNO = 0         
     }
     
     // Weight Ticket Number
     if (mi.in.get("WTKN") != null) {
        inWTKN = mi.in.get("WTKN") 
     } else {
        inWTKN = ""         
     }

     // Weight Date
     if (mi.in.get("WTDT") != null) {
        inWTDT = mi.in.get("WTDT") 
     }
   
     // Weight Location Number
     if (mi.in.get("WTLN") != null) {
        inWTLN= mi.in.get("WTLN") 
     } else {
        inWTLN = ""         
     }
           
     // Delivery Number
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0        
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


     // Validate Weight Ticket Line record
     Optional<DBContainer> EXTDWT = findEXTDWT(inCONO, inDIVI, inWTNO, inDLNO)
     if (!EXTDWT.isPresent()) {
        mi.error("Weight Ticket Line doesn't exist")   
        return             
     } else {
        // Update record
        updEXTDWTRecord()
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
  // Update EXTDWT record
  //********************************************************************    
  void updEXTDWTRecord(){      
     DBAction action = database.table("EXTDWT").index("00").build()
     DBContainer EXTDWT = action.getContainer()     
     EXTDWT.set("EXCONO", inCONO)
     EXTDWT.set("EXDIVI", inDIVI)
     EXTDWT.set("EXWTNO", inWTNO)
     EXTDWT.set("EXDLNO", inDLNO)

     // Read with lock
     action.readLock(EXTDWT, updateCallBackEXTDWT)
     }
   
     Closure<?> updateCallBackEXTDWT = { LockedResult lockedResult -> 
       if (mi.in.get("WTKN") != null) {
          lockedResult.set("EXWTKN", mi.inData.get("WTKN").trim())
       }
       
       if (mi.in.get("WTDT") != null) {
          lockedResult.set("EXWTDT", mi.in.get("WTDT"))
       }
  
       if (mi.in.get("WTLN") != null) {
          lockedResult.set("EXWTLN", mi.inData.get("WTLN").trim())
       }
       
       if (mi.in.get("GRWE") != null) {
          lockedResult.set("EXGRWE", mi.in.get("GRWE"))
       }
  
       if (mi.in.get("TRWE") != null) {
          lockedResult.set("EXTRWE", mi.in.get("TRWE"))
       }
       
       if (mi.in.get("NEWE") != null) {
          lockedResult.set("EXNEWE", mi.in.get("NEWE"))
       }
      
       int changeNo = lockedResult.get("EXCHNO")
       int newChangeNo = changeNo + 1 
       int changedate = utility.call("DateUtil", "currentDateY8AsInt")
       lockedResult.set("EXLMDT", changedate)        
       lockedResult.set("EXCHNO", newChangeNo) 
       lockedResult.set("EXCHID", program.getUser())
       lockedResult.update()
    }

} 

