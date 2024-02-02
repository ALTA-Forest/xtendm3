// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to update a contract instruction in EXTDLH
// Transaction UpdDelivery
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
 * @param: DLTP - Instruction Code
 * @param: STAT - Display Order
 * @param: DLDT - Delivery Date
 * @param: SUNO - Supplier
 * @param: BUYE - Buyer
 * @param: DLTY - Deliver To Yard
 * @param: RCPN - Receipt Number
 * @param: TRPN - Trip Ticket Number
 * @param: TRCK - Truck Number
 * @param: BRND - Brand
 * @param: DLST - Delivery Sub Type
 * @param: CTNO - Contract Number
 * @param: EROR - Errors
 * @param: ALHA - Alternate Hauler
 * @param: ISZP - Zero Payment
 * @param: FACI - Facility
 * @param: RVID - Revision Id
 * @param: ISPS - Payee Split
 * @param: VLDT - Validate Date
 * @param: NOTE - Notes
 * 
*/



public class UpdDelivery extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inDLNO
  int inDLTP
  int inSTAT
  int inDLDT
  String inSUNO
  String inBUYE
  String inDLFY
  String inDLTY
  int inFDCK
  int inTDCK
  String inRCPN
  String inTRPN
  String inTRCK
  String inBRND
  int inDLST
  int inCTNO
  String inNOTE
  int inALHA
  int inISZP
  String inFACI
  String inRVID
  int inISPS
  String inVLDT
  String inEROR
  
  // Constructor 
  public UpdDelivery(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
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

     // Delivery Number
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0         
     }

     // Delivery Type
     if (mi.in.get("DLTP") != null) {
        inDLTP = mi.in.get("DLTP") 
     } 
           
     // Status
     if (mi.in.get("STAT") != null) {
        inSTAT = mi.in.get("STAT") 
     } 
     
     // Delivery Date
     if (mi.in.get("DLDT") != null) {
        inDLDT = mi.in.get("DLDT") 
     } 

     // Supplier
     if (mi.in.get("SUNO") != null) {
        inSUNO = mi.in.get("SUNO") 
     } else {
        inSUNO = ""        
     }

     // Buyer
     if (mi.in.get("BUYE") != null) {
        inBUYE = mi.in.get("BUYE") 
     } else {
        inBUYE = ""      
     }

     // Deliver From Yard
     if (mi.in.get("DLFY") != null) {
        inDLFY = mi.in.get("DLFY") 
     } else {
        inDLFY = ""        
     }

     // Deliver To Yard
     if (mi.in.get("DLTY") != null) {
        inDLTY = mi.in.get("DLTY") 
     } else {
        inDLTY = ""        
     }

     // From Deck
     if (mi.in.get("FDCK") != null) {
        inFDCK = mi.in.get("FDCK") 
     } 

     // To Deck
     if (mi.in.get("TDCK") != null) {
        inTDCK = mi.in.get("TDCK") 
     } 

     // Receipt Number
     if (mi.in.get("RCPN") != null) {
        inRCPN = mi.in.get("RCPN") 
     } else {
        inRCPN = ""        
     }

     // Trip Ticket Number
     if (mi.in.get("TRPN") != null) {
        inTRPN = mi.in.get("TRPN") 
     } else {
        inTRPN = ""        
     }
     
     // Truck Number
     if (mi.in.get("TRCK") != null) {
        inTRCK = mi.in.get("TRCK") 
     } else {
        inTRCK = ""        
     }

     // Brand
     if (mi.in.get("BRND") != null) {
        inBRND = mi.in.get("BRND") 
     } else {
        inBRND = ""        
     }

     // Delivery Sub Type
     if (mi.in.get("DLST") != null) {
        inDLST = mi.in.get("DLST") 
     } 
     
     // Contract Number
     if (mi.in.get("CTNO") != null) {
        inCTNO = mi.in.get("CTNO") 
     }
               
     // Alternate Hauler
     if (mi.in.get("ALHA") != null) {
        inALHA = mi.in.get("ALHA") 
     } 

     // Zero Payment
     if (mi.in.get("ISZP") != null) {
        inISZP = mi.in.get("ISZP") 
     } 
     
     // Facility
     if (mi.in.get("FACI") != null) {
        inFACI = mi.in.get("FACI") 
     } else {
        inFACI = ""        
     }
     
     // Revision Id
     if (mi.in.get("RVID") != null) {
        inRVID = mi.in.get("RVID") 
     } else {
        inRVID = ""        
     }
     
     // Payee Split
     if (mi.in.get("ISPS") != null) {
        inISPS = mi.in.get("ISPS") 
     } 
     
     // Validate Date
     if (mi.in.get("VLDT") != null) {
        inVLDT = mi.in.get("VLDT") 
     } else {
        inVLDT = ""        
     }

     // Notes
     if (mi.in.get("NOTE") != null) {
        inNOTE = mi.in.get("NOTE") 
     } else {
        inNOTE = ""        
     }
     
     // Errors
     if (mi.in.get("EROR") != null) {
        inEROR = mi.in.get("EROR") 
     } else {
        inEROR = ""        
     }

     // Validate Delivery Head record
     Optional<DBContainer> EXTDLH = findEXTDLH(inCONO, inDIVI, inDLNO)
     if (!EXTDLH.isPresent()) {
        mi.error("Delivery doesn't exists")   
        return             
     } else {    
        // Update record
        updEXTDLHRecord()
     }
     
  }
  
  //******************************************************************** 
  // Get EXTDLH record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDLH(int CONO, String DIVI, int DLNO){  
     DBAction query = database.table("EXTDLH").index("00").build()
     DBContainer EXTDLH = query.getContainer()
     EXTDLH.set("EXCONO", CONO)
     EXTDLH.set("EXDIVI", DIVI)
     EXTDLH.set("EXDLNO", DLNO)
     if(query.read(EXTDLH))  { 
       return Optional.of(EXTDLH)
     } 
  
     return Optional.empty()
  }


  //******************************************************************** 
  // Update EXTDLH record
  //********************************************************************    
  void updEXTDLHRecord(){      
     DBAction action = database.table("EXTDLH").index("00").build()
     DBContainer EXTDLH = action.getContainer()    
     EXTDLH.set("EXCONO", inCONO)
     EXTDLH.set("EXDIVI", inDIVI)
     EXTDLH.set("EXDLNO", inDLNO)

     // Read with lock
     action.readLock(EXTDLH, updateCallBackEXTDLH)
     }
   
     Closure<?> updateCallBackEXTDLH = { LockedResult lockedResult -> 
       if (mi.in.get("DLTP") != null) {
          lockedResult.set("EXDLTP", mi.in.get("DLTP"))
       }
       
       if (mi.in.get("STAT") != null) {
          lockedResult.set("EXSTAT", mi.in.get("STAT"))
       }
  
       if (mi.in.get("DLDT") != null) {
          lockedResult.set("EXDLDT", mi.in.get("DLDT"))
       }
  
       if (inSUNO != "") {
          lockedResult.set("EXSUNO", inSUNO)
       }
  
       if (inBUYE != "") {
          lockedResult.set("EXBUYE", inBUYE)
       }
  
       if (inDLFY != "") {
          lockedResult.set("EXDLFY", inDLFY)
       }
  
       if (inDLTY != "") {
          lockedResult.set("EXDLTY", inDLTY)
       }
       
       if (mi.in.get("FDCK") != null) {
          lockedResult.set("EXFDCK", mi.in.get("FDCK"))
       }
  
       if (mi.in.get("TDCK") != null) {
          lockedResult.set("EXTDCK", mi.in.get("TDCK"))
       }
       
       if (inRCPN != "") {
          lockedResult.set("EXRCPN", inRCPN)
       }
       
       if (inTRPN != "") {
          lockedResult.set("EXTRPN", inTRPN)
       }
       
       if (inTRCK != "") {
          lockedResult.set("EXTRCK", inTRCK)
       }
  
       if (inBRND != "") {
          lockedResult.set("EXBRND", inBRND)
       }
       
       if (mi.in.get("DLST") != null) {
          lockedResult.set("EXDLST", mi.in.get("DLST"))
       }
       
       if (mi.in.get("CTNO") != null) {
          lockedResult.set("EXCTNO", mi.in.get("CTNO"))
       }
  
       if (mi.in.get("ALHA") != null) {
          lockedResult.set("EXALHA", inALHA)
       }
       
       if (mi.in.get("ISZP") != null) {
          lockedResult.set("EXISZP", mi.in.get("ISZP"))
       }
       
       if (inFACI != "" && inFACI != null) {
          lockedResult.set("EXFACI", inFACI)
       }
       
       if (inRVID != "" && inRVID != null) {
          lockedResult.set("EXRVID", inRVID)
       }
       
       if (mi.in.get("ISPS") != null) {
          lockedResult.set("EXISPS", mi.in.get("ISPS"))
       }
       
       if (inVLDT != "" && inVLDT != null) {
          lockedResult.set("EXVLDT", inVLDT)
       }
  
       if (inNOTE != "") {
          lockedResult.set("EXNOTE", inNOTE)
       }
       
        if (inEROR != "") {
          lockedResult.set("EXEROR", inEROR)
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

