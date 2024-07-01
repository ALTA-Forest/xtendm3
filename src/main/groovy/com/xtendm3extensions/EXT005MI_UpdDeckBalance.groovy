// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2024-05-16
// @version   1.0 
//
// Description 
// This API is will be used to update the balance in EXTDPR/ESTDPD
// Transaction ValDeckBalance
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DPID - Deck ID
*/



public class UpdDeckBalance extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  Integer inCONO
  String inDIVI
  int inDPID
  int inTRNO
  double netBF
  double netBalance
  int deckID
  int transactionNumber
  double sumNetBF


  // Constructor 
  public UpdDeckBalance(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
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

     // Deck ID
     if (mi.in.get("DPID") != null) {
        inDPID = mi.in.get("DPID") 
     } else {
        inDPID = 0         
     }


     // Validate Deck Head record
     Optional<DBContainer> EXTDPH = findEXTDPH(inCONO, inDIVI, inDPID)
     if(!EXTDPH.isPresent()){
        mi.error("Deck Head record doesn't exist")   
        return             
     }
     
     //List all transactions for the input deck
     netBF = 0d
     netBalance = 0d
     sumNetBF = 0d
     deckID = 0
     transactionNumber = 0
     List<DBContainer> resultEXTDPR = listEXTDPR(inCONO, inDIVI, inDPID) 
     for (DBContainer recLineEXTDPR : resultEXTDPR) { 
          //For each transaction, calculate and update the TRNB value
          deckID = recLineEXTDPR.get("EXDPID")
          transactionNumber = recLineEXTDPR.get("EXTRNO")
          logger.debug("transactionNumber ${transactionNumber}")
          netBF = recLineEXTDPR.get("EXTNBF")
          
          sumNetBF = netBalance + netBF
          
          netBalance = recLineEXTDPR.get("EXTRNB")

          //Update TRNB for each transaction number
          updEXTDPRRecord()
     }

     //Update EXTDPD with correct value
     updEXTDPDRecord()

     mi.outData.put("CONO", String.valueOf(inCONO)) 
     mi.outData.put("DIVI", inDIVI) 
     mi.outData.put("TRNO", String.valueOf(transactionNumber))      
     mi.outData.put("TRNB", String.valueOf(sumNetBF))      
     mi.write()
  }


   //******************************************************************** 
   // Get EXTDPD record
   //******************************************************************** 
   private Optional<DBContainer> findEXTDPD(int CONO, String DIVI, int DPID) {  
      DBAction query = database.table("EXTDPD").index("00").selection("EXNVBF").build()
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
      DBAction query = database.table("EXTDPH").index("00").build()
      DBContainer EXTDPH = query.getContainer()
      EXTDPH.set("EXCONO", CONO)
      EXTDPH.set("EXDIVI", DIVI)
      EXTDPH.set("EXDPID", DPID)
      if(query.read(EXTDPH))  { 
        return Optional.of(EXTDPH)
      } 

      return Optional.empty()
   }



  //******************************************************************** 
  // Get all transactions from input deck
  //********************************************************************  
  private List<DBContainer> listEXTDPR(int CONO, String DIVI, int DPID){
    List<DBContainer>recLineEXTDPR = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("EXTDPR")
    expression = expression.eq("EXCONO", String.valueOf(CONO)).and(expression.eq("EXDIVI", DIVI)).and(expression.eq("EXDPID", String.valueOf(DPID)))
    
    DBAction query = database.table("EXTDPR").index("00").matching(expression).selection("EXDPID", "EXTRNO", "EXTNBF", "EXTRNB").build()
    DBContainer EXTDPR = query.createContainer()
    EXTDPR.set("EXCONO", CONO)
    EXTDPR.set("EXDIVI", DIVI)
    EXTDPR.set("EXDPID", DPID)

    int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords() 
    query.readAll(EXTDPR, 3, pageSize, { DBContainer recordEXTDPR ->  
       recLineEXTDPR.add(recordEXTDPR.createCopy()) 
    })

    return recLineEXTDPR
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
     EXTDPR.set("EXTRNO", transactionNumber)

     // Read with lock
     action.readLock(EXTDPR, updateCallBackEXTDPR)
     }
   
     Closure<?> updateCallBackEXTDPR = { LockedResult lockedResult -> 
       lockedResult.set("EXTRNB", sumNetBF)

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
  void updEXTDPDRecord(){      
     DBAction action = database.table("EXTDPD").index("00").build()
     DBContainer EXTDPD = action.getContainer()     
     EXTDPD.set("EXCONO", inCONO)
     EXTDPD.set("EXDIVI", inDIVI)
     EXTDPD.set("EXDPID", inDPID)

     // Read with lock
     action.readLock(EXTDPD, updateCallBackEXTDPD)
     }
   
     Closure<?> updateCallBackEXTDPD = { LockedResult lockedResult -> 
       lockedResult.set("EXNVBF", sumNetBF)
  
       // Update changed information
       int changeNo = lockedResult.get("EXCHNO")
       int newChangeNo = changeNo + 1 
       int changeddate = utility.call("DateUtil", "currentDateY8AsInt")
       lockedResult.set("EXLMDT", changeddate)          
       lockedResult.set("EXCHNO", newChangeNo) 
       lockedResult.set("EXCHID", program.getUser())
       lockedResult.update()
    }

     
} 

