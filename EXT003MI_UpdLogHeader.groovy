// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to update a log header in EXTSLH
// Transaction UpdLogHeader
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

// Date         Changed By                         Description
// 2023-05-10   Jessica Bjorklund (Columbus)       Creation
// 2024-07-01   Jessica Bjorklund (Columbus)       Change of update of SPEC, ECOD and TGNO to allow blank value
// 2024-07-19   Jessica Bjorklund (Columbus)       Add DSID and SRID as input fields


/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: STID - Scale Ticket ID
 * @param: STNO - Scale Ticket Number
 * @param: SEQN - Log Number
 * @param: TDCK - To Deck
 * @param: SPEC - Species
 * @param: ECOD - Exception Code
 * @param: TGNO - Tag Number
 * @param: LAMT - Amount
 * @param: DSID - Section ID
 * @param: SRID - Rate ID
*/



public class UpdLogHeader extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inSTID
  int inSEQN  
  int inTDCK 
  String inSPEC 
  String inECOD  
  String inTGNO
  double inLAMT
  int inDSID 
  int inSRID

  
  // Constructor 
  public UpdLogHeader(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, UtilityAPI utility, ProgramAPI program, LoggerAPI logger) {
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

     // Scale Ticket ID
     if (mi.in.get("STID") != null) {
        inSTID = mi.in.get("STID") 
     } else {
        inSTID = 0         
     }

     // Log Number
     if (mi.in.get("SEQN") != null) {
        inSEQN = mi.in.get("SEQN") 
     } else {
        inSEQN = 0        
     }
           
     // To Deck
     if (mi.in.get("TDCK") != null) {
        inTDCK = mi.in.get("TDCK") 
     } 
 
      // Species
     if (mi.in.get("SPEC") != null ) {
        inSPEC = mi.inData.get("SPEC").trim() 
     }
     
     // Exception Code
     if (mi.in.get("ECOD") != null) {
        inECOD = mi.in.get("ECOD") 
     } 

     // Tag Number
     if (mi.in.get("TGNO") != null) {
        inTGNO = mi.in.get("TGNO") 
     }

     // Amount
     if (mi.in.get("LAMT") != null) {
        inLAMT = mi.in.get("LAMT") 
     } 

     // Section ID
     if (mi.in.get("DSID") != null) {
        inDSID = mi.in.get("DSID") 
     }

     // Rate ID
     if (mi.in.get("SRID") != null) {
        inSRID = mi.in.get("SRID") 
     } 


     // Validate Log Header Line record
     Optional<DBContainer> EXTSLH = findEXTSLH(inCONO, inDIVI, inSTID, inSEQN)
     if (!EXTSLH.isPresent()) {
        mi.error("Log Header doesn't exist")   
        return             
     } else {
        // Update record
        updEXTSLHRecord()
     }
     
  }
  
    
  //******************************************************************** 
  // Get EXTSLH record
  //******************************************************************** 
  private Optional<DBContainer> findEXTSLH(int CONO, String DIVI, int STID, int SEQN){  
     DBAction query = database.table("EXTSLH").index("00").build()
     DBContainer EXTSLH = query.getContainer()
     EXTSLH.set("EXCONO", CONO)
     EXTSLH.set("EXDIVI", DIVI)
     EXTSLH.set("EXSTID", STID)
     EXTSLH.set("EXSEQN", SEQN)
     if(query.read(EXTSLH))  { 
       return Optional.of(EXTSLH)
     } 
  
     return Optional.empty()
  }

  //******************************************************************** 
  // Update EXTSLH record
  //********************************************************************    
  void updEXTSLHRecord(){      
     DBAction action = database.table("EXTSLH").index("00").build()
     DBContainer EXTSLH = action.getContainer()     
     EXTSLH.set("EXCONO", inCONO)
     EXTSLH.set("EXDIVI", inDIVI)
     EXTSLH.set("EXSTID", inSTID)
     EXTSLH.set("EXSEQN", inSEQN)

     // Read with lock
     action.readLock(EXTSLH, updateCallBackEXTSLH)
     }
   
     Closure<?> updateCallBackEXTSLH = { LockedResult lockedResult -> 
       if (mi.in.get("TDCK") != null) {
          lockedResult.set("EXTDCK", mi.in.get("TDCK"))
       }
  
       if (inSPEC != null) {
          lockedResult.set("EXSPEC", inSPEC)
       }
   
       if (inECOD != null) {
          lockedResult.set("EXECOD", inECOD)
       }
       
       if (inTGNO != null) {
          lockedResult.set("EXTGNO", inTGNO)
       }
  
       if (mi.in.get("LAMT") != null) {
          lockedResult.set("EXLAMT", mi.in.get("LAMT"))
       }
       
       if (mi.in.get("DSID") != null) {
          lockedResult.set("EXDSID", mi.in.get("DSID"))
       }

       if (mi.in.get("SRID") != null) {
          lockedResult.set("EXSRID", mi.in.get("SRID"))
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

