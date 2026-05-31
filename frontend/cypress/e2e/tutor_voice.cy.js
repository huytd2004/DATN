describe('AI Tutor voice flow', () => {
  it('can start session and upload audio', () => {
    // Visit the app
    cy.visit('/')

    // Navigate to Tutor (assumes route exists)
    cy.contains('AI Tutor').click()

    // This test assumes the UI allows creating a session quickly.
    // We will stub API responses to focus on frontend flow.
    cy.intercept('POST', '/api/v1/tutor/sessions', {
      statusCode: 201,
      body: { data: { sessionId: 'test-session', initialMessage: { id: 'm1', role: 'assistant', content: 'はじめまして', audioUrl: '/api/v1/tutor/audio/test-session/tts.wav' } }, status: 'success' }
    }).as('createSession')

    cy.intercept('POST', '/api/v1/tutor/sessions/test-session/messages', (req) => {
      req.reply({
        statusCode: 200,
        body: { data: { message: { id: 'm2', role: 'assistant', content: 'いいですね', audioUrl: '/api/v1/tutor/audio/test-session/tts2.wav' } }, status: 'success' }
      })
    }).as('sendMessage')

    // Start session: interact with UI elements (selectors may vary)
    cy.get('button').contains('Start').click({ force: true })

    // Simulate recording upload by directly calling the store action via window (if exposed)
    // Fallback: use UI to select file input if available
    const blob = new Blob(['audio test'], { type: 'audio/webm' })
    const file = new File([blob], 'recording.webm', { type: 'audio/webm' })

    // Find file input and attach file
    cy.get('input[type=file]').attachFile({ fileContent: file, fileName: 'recording.webm', mimeType: 'audio/webm' })

    // Click send
    cy.get('button').contains('send').click({ force: true })

    cy.wait('@sendMessage')
    cy.contains('いいですね')
  })
})
